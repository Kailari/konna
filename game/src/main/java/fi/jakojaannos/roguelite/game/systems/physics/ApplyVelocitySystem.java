package fi.jakojaannos.roguelite.game.systems.physics;

import org.joml.Math;
import org.joml.Vector2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.GJK2D;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import fi.jakojaannos.roguelite.game.data.components.Shape;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.resources.collision.Colliders;
import fi.jakojaannos.roguelite.game.data.resources.collision.Collisions;
import fi.jakojaannos.roguelite.game.systems.collision.Collision;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionEvent;

/**
 * Applies velocity read from the {@link Velocity} component to character {@link Transform}, handling collisions and
 * firing {@link CollisionEvent Collision Events} whenever necessary. Backbone of the physics and collision detection of
 * characters and other simple moving entities.
 *
 * @see CollisionEvent
 * @see Collision
 */
public class ApplyVelocitySystem implements EcsSystem<ApplyVelocitySystem.Resources, ApplyVelocitySystem.EntityData, EcsSystem.NoEvents> {
    private static final Logger LOG = LoggerFactory.getLogger(ApplyVelocitySystem.class);
    /**
     * If velocity length is smaller than this value, applying velocity will be skipped.
     */
    private static final double VELOCITY_EPSILON = 0.0000001;
    /**
     * Maximum tries per tick per entity we may attempt to move.
     */
    private static final int MAX_ITERATIONS = 25;
    /**
     * Should an entity move less than this value during an movement iteration, we may consider it being still and can
     * stop trying to move.
     */
    private static final double MOVE_EPSILON = 0.00001;
    /**
     * The size of a single movement step. When near collision, this is the resolution at which entities are allowed to
     * move.
     */
    private static final double STEP_SIZE = 0.01;

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var collisionEvents = resources.collisions;
        final var delta = resources.timeManager.getTimeStepInSeconds();

        final var tileMapLayers = resources.colliders.tileMapLayersWithCollision;

        // XXX: I'm fairly confident that this breaks determinism. Parallel execution may impose
        //      race conditions where certain fast-moving entities which collide with each other
        //      are handled at the same time. In these cases, the collision response might depend
        //      on which one of the entities is handled first.
        entities.forEach(entity -> {
            final var collisionTargets = new ArrayList<CollisionCandidate>();
            final var overlapTargets = new ArrayList<CollisionCandidate>();
            final var transform = entity.getData().transform;
            final var velocity = entity.getData().velocity;
            final var maybeCollider = entity.getData().collider;

            if (velocity.length() < VELOCITY_EPSILON) {
                return;
            }

            if (maybeCollider.isPresent()) {
                final var collider = maybeCollider.get();

                final var translatedCollider = new StretchedCollider(collider, transform);
                final var translatedTransform = new Transform(transform);
                translatedTransform.position.add(velocity.mul(delta, new Vector2d()));
                translatedCollider.refreshTranslatedVertices(translatedTransform);
                final var bounds = translatedCollider.getBounds(translatedTransform);

                collectRelevantTiles(tileMapLayers,
                                     translatedTransform,
                                     translatedCollider,
                                     collisionTargets::add
                );
                collectRelevantEntities(resources.colliders,
                                        entity,
                                        collider.layer,
                                        collisionTargets::add,
                                        overlapTargets::add);

                moveWithCollision(collisionEvents,
                                  entity,
                                  transform,
                                  velocity,
                                  translatedCollider,
                                  translatedTransform,
                                  collisionTargets,
                                  overlapTargets,
                                  delta);
            } else {
                moveWithoutCollision(transform, velocity, delta);
            }
        });
    }

    private void collectRelevantTiles(
            final Collection<TileMap<TileType>> tileMapLayers,
            final Transform translatedTransform,
            final Shape translatedCollider,
            final Consumer<CollisionCandidate> colliderConsumer
    ) {
        final var bounds = translatedCollider.getBounds(translatedTransform);
        final var startX = (int) Math.floor(bounds.minX - STEP_SIZE);
        final var startY = (int) Math.floor(bounds.minY - STEP_SIZE);
        final var endX = (int) Math.ceil(bounds.maxX + STEP_SIZE);
        final var endY = (int) Math.ceil(bounds.maxY + STEP_SIZE);

        final var width = endX - startX;
        final var height = endY - startY;
        for (var ix = 0; ix < width; ++ix) {
            for (var iy = 0; iy < height; ++iy) {
                final var x = startX + ix;
                final var y = startY + iy;

                for (final var layer : tileMapLayers) {
                    final var tileType = layer.getTile(x, y);
                    if (tileType.solid()) {
                        colliderConsumer.accept(new CollisionCandidate(x, y));
                        break;
                    }
                }
            }
        }
    }

    private void moveWithCollision(
            final Collisions collisionEvents,
            final EntityDataHandle<EntityData> entity,
            final Transform transform,
            final Velocity velocity,
            final StretchedCollider translatedCollider,
            final Transform translatedTransform,
            final Collection<CollisionCandidate> collisionTargets,
            final Collection<CollisionCandidate> overlapTargets,
            final double delta
    ) {
        var distanceRemaining = velocity.mul(delta, new Vector2d())
                                        .length();
        final var direction = velocity.normalize(new Vector2d());

        var iterations = 0;
        final var collisions = new ArrayList<CollisionEventCandidate>();
        final var collisionsX = new ArrayList<CollisionEventCandidate>();
        final var collisionsY = new ArrayList<CollisionEventCandidate>();
        final var tmpTransform = new Transform(transform);
        final var tmpTransformX = new Transform(transform);
        final var tmpTransformY = new Transform(transform);

        while (distanceRemaining > 0 && (iterations++) < MAX_ITERATIONS) {
            collisions.clear();
            collisionsX.clear();
            collisionsY.clear();
            tmpTransform.set(transform);
            tmpTransformX.set(transform);
            tmpTransformY.set(transform);

            // Refresh non-translated vertices of the collider
            translatedCollider.refresh();

            var distanceMoved = moveUntilCollision(
                    tmpTransform,
                    translatedTransform,
                    translatedCollider,
                    direction,
                    distanceRemaining,
                    collisionTargets,
                    overlapTargets,
                    (candidate, mode) ->
                            collisions.add(new CollisionEventCandidate(mode, candidate))
            );

            var actualCollisions = collisions;
            if (distanceMoved < MOVE_EPSILON) {
                var distanceMovedX = 0.0;
                if (Math.abs(direction.x) > VELOCITY_EPSILON) {
                    distanceMovedX = moveUntilCollision(
                            tmpTransformX,
                            translatedTransform,
                            translatedCollider,
                            new Vector2d(direction.x, 0.0).normalize(),
                            Math.abs(distanceRemaining * direction.x),
                            collisionTargets,
                            overlapTargets,
                            (candidate, mode) ->
                                    collisionsX.add(new CollisionEventCandidate(mode, candidate)));
                }

                var distanceMovedY = 0.0;
                if (Math.abs(direction.y) > VELOCITY_EPSILON) {
                    distanceMovedY = moveUntilCollision(
                            tmpTransformY,
                            translatedTransform,
                            translatedCollider,
                            new Vector2d(0.0, direction.y).normalize(),
                            Math.abs(distanceRemaining * direction.y),
                            collisionTargets,
                            overlapTargets,
                            (candidate, mode) ->
                                    collisionsY.add(new CollisionEventCandidate(mode, candidate)));
                }

                if (distanceMovedX > MOVE_EPSILON) {
                    distanceMoved = distanceMovedX;
                    tmpTransform.set(tmpTransformX);
                    actualCollisions = collisionsY;
                } else if (distanceMovedY > MOVE_EPSILON) {
                    distanceMoved = distanceMovedY;
                    tmpTransform.set(tmpTransformY);
                    actualCollisions = collisionsX;
                }
            }

            for (final var collision : actualCollisions) {
                fireCollisionEvent(collisionEvents,
                                   entity,
                                   collision.candidate,
                                   collision.mode);
            }

            if (distanceMoved < MOVE_EPSILON) {
                break;
            }

            transform.set(tmpTransform);
            distanceRemaining -= distanceMoved;
        }
    }

    private double moveUntilCollision(
            final Transform transform,
            final Transform translatedTransform,
            final StretchedCollider translatedCollider,
            final Vector2d direction,
            final double distance,
            final Collection<CollisionCandidate> collisionTargets,
            final Collection<CollisionCandidate> overlapTargets,
            final BiConsumer<CollisionCandidate, Collision.Mode> collisionConsumer
    ) {
        final var maybeCollision = collisionsAfterMoving(distance,
                                                         direction,
                                                         transform,
                                                         translatedTransform,
                                                         translatedCollider,
                                                         collisionTargets);
        final double actualDistance;
        final Vector2d actualDirection;
        if (maybeCollision.isPresent()) {
            final var collision = maybeCollision.get();
            final var translation = new Vector2d(direction).mul(distance)
                                                           .sub(collision.collision()
                                                                         .normal()
                                                                         .mul(collision.collision()
                                                                                       .depth(),
                                                                              new Vector2d()));
            actualDistance = translation.length();
            actualDirection = translation.normalize();
        } else {
            actualDistance = distance;
            actualDirection = direction;
        }

        moveDistanceTriggeringCollisions(transform,
                                         actualDirection,
                                         actualDistance,
                                         translatedTransform,
                                         translatedCollider,
                                         overlapTargets,
                                         collisionConsumer);

        return actualDistance;
    }

    private void moveDistanceTriggeringCollisions(
            final Transform transform,
            final Vector2d direction,
            final double distanceToMove,
            final Transform translatedTransform,
            final StretchedCollider translatedCollider,
            final Collection<CollisionCandidate> overlapTargets,
            final BiConsumer<CollisionCandidate, Collision.Mode> collisionConsumer
    ) {
        translatedTransform.position.set(transform.position)
                                    .add(direction.mul(distanceToMove, new Vector2d()));

        translatedCollider.refreshTranslatedVertices(translatedTransform);
        for (final var candidate : overlapTargets) {
            if (candidate.overlaps(translatedTransform, translatedCollider).collides()) {
                collisionConsumer.accept(candidate, Collision.Mode.OVERLAP);
            }
        }

        final var translation = direction.mul(distanceToMove, new Vector2d());
        transform.position.add(translation);
    }

    private Optional<ActualCollision> collisionsAfterMoving(
            final double distance,
            final Vector2d direction,
            final Transform transform,
            final Transform translatedTransform,
            final StretchedCollider translatedCollider,
            final Collection<CollisionCandidate> collisionTargets
    ) {
        translatedTransform.position.set(transform.position)
                                    .add(direction.mul(distance, new Vector2d()));

        translatedCollider.refreshTranslatedVertices(translatedTransform);
        for (final var target : collisionTargets) {
            final var collision = target.overlaps(translatedTransform, translatedCollider);
            if (collision.collides()) {
                return Optional.of(new ActualCollision(target, collision));
            }
        }

        return Optional.empty();
    }

    private void fireCollisionEvent(
            final Collisions collisions,
            final EntityDataHandle<EntityData> entity,
            final CollisionCandidate candidate,
            final Collision.Mode mode
    ) {
        if (candidate.entity == null) {
            final var event = new CollisionEvent(Collision.tile(mode,
                                                                candidate.transform.position.x,
                                                                candidate.transform.position.y));
            collisions.fireCollisionEvent(entity.getHandle(), event);
        } else {
            final var event = new CollisionEvent(Collision.entity(mode, candidate.entity.entity()));
            final var otherEvent = new CollisionEvent(Collision.entity(mode, entity.getHandle()));
            collisions.fireCollisionEvent(entity.getHandle(), event);
            collisions.fireCollisionEvent(candidate.entity.entity(), otherEvent);

            candidate.entity.entity().addOrGet(RecentCollisionTag.class, RecentCollisionTag::new);
        }

        entity.addOrGet(RecentCollisionTag.class, RecentCollisionTag::new);
    }

    private void moveWithoutCollision(
            final Transform transform,
            final Velocity velocity,
            final double delta
    ) {
        transform.position.add(velocity.mul(delta, new Vector2d()));
    }

    private static void collectRelevantEntities(
            final Colliders colliders,
            final EntityDataHandle<EntityData> entity,
            final CollisionLayer layer,
            final Consumer<CollisionCandidate> colliderConsumer,
            final Consumer<CollisionCandidate> overlapConsumer
    ) {
        final var potentialCollisions = colliders.solidForLayer.get(layer);
        if (potentialCollisions != null) {
            for (final var other : potentialCollisions) {
                if (other.entity().getId() == entity.getId()) {
                    continue;
                }
                colliderConsumer.accept(new CollisionCandidate(other));
            }
        }

        final var potentialOverlaps = colliders.overlapsWithLayer.get(layer);
        if (potentialOverlaps != null) {
            for (final var other : potentialOverlaps) {
                if (other.entity().getId() == entity.getId()) {
                    continue;
                }
                overlapConsumer.accept(new CollisionCandidate(other));
            }
        }
    }

    public static record Resources(
            Collisions collisions,
            Colliders colliders,
            TimeManager timeManager
    ) {}

    public static record EntityData(
            Transform transform,
            Velocity velocity,
            Optional<Collider>collider
    ) {}

    private static record ActualCollision(CollisionCandidate candidate, GJK2D.Result collision) {}

    public static final class CollisionCandidate {
        private static final Vector2d[] TILE_VERTICES = new Vector2d[]{
                new Vector2d(0, 0),
                new Vector2d(1, 0),
                new Vector2d(0, 1),
                new Vector2d(1, 1)
        };
        private static final Shape TILE_SHAPE = ignored -> TILE_VERTICES;

        private final Transform transform;
        @Nullable private final Colliders.ColliderEntity entity;

        public CollisionCandidate(
                final double x,
                final double y
        ) {
            this.transform = new Transform(x, y);
            this.entity = null;
        }

        public CollisionCandidate(
                final Colliders.ColliderEntity entity
        ) {
            this.entity = entity;
            this.transform = entity.transform();
        }

        public GJK2D.Result overlaps(final Transform transform, final Shape shape) {
            return GJK2D.getCollision(transform,
                                      shape,
                                      this.transform,
                                      this.entity != null
                                              ? this.entity.collider()
                                              : TILE_SHAPE);
        }
    }

    private static record CollisionEventCandidate(
            Collision.Mode mode,
            CollisionCandidate candidate
    ) {
    }

    private static class StretchedCollider implements Shape {
        private static final Vector2d tmpDelta = new Vector2d();

        private final Collider collider;
        private final Transform transform;
        private final Vector2d[] vertices = new Vector2d[]{
                new Vector2d(), new Vector2d(), new Vector2d(), new Vector2d(), new Vector2d(),
                new Vector2d(), new Vector2d(), new Vector2d()
        };

        StretchedCollider(final Collider collider, final Transform transform) {
            this.collider = collider;
            this.transform = transform;

            refresh();
        }

        private void refresh() {
            final var vertices = this.collider.getVerticesInLocalSpace(this.transform);
            this.vertices[0].set(vertices[0]);
            this.vertices[1].set(vertices[1]);
            this.vertices[2].set(vertices[2]);
            this.vertices[3].set(vertices[3]);
        }

        public void refreshTranslatedVertices(final Transform translatedTransform) {
            final var delta = translatedTransform.position.sub(this.transform.position, tmpDelta);
            final var translatedVertices = this.collider.getVerticesInLocalSpace(translatedTransform);
            this.vertices[4].set(translatedVertices[0]).add(delta);
            this.vertices[5].set(translatedVertices[1]).add(delta);
            this.vertices[6].set(translatedVertices[2]).add(delta);
            this.vertices[7].set(translatedVertices[3]).add(delta);
        }

        @Override
        public final Vector2d[] getVerticesInLocalSpace(final Transform ignored) {
            return this.vertices;
        }
    }
}
