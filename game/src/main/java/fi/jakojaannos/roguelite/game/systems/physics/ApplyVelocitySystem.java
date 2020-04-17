package fi.jakojaannos.roguelite.game.systems.physics;

import org.joml.Math;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    /**
     * If velocity length is smaller than this value, applying velocity will be skipped.
     */
    private static final double VELOCITY_EPSILON = 0.0000001;
    /**
     * Maximum tries per tick per entity we may attempt to move.
     */
    private static final int MAX_ITERATIONS = 2;
    /**
     * Should an entity move less than this value during an movement iteration, we may consider it being still and can
     * stop trying to move.
     */
    private static final double MOVE_EPSILON = 0.00001;
    /**
     * The minimum size of a "try move" step. Fast-moving entities may occasionally pass through walls that are thinner
     * than this.
     */
    private static final double STEP_SIZE = 0.25;

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
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

            if (velocity.lengthSquared() < VELOCITY_EPSILON) {
                return;
            }

            if (maybeCollider.isPresent()) {
                final var collider = maybeCollider.get();

                final var translatedCollider = new StretchedCollider(collider, transform);
                final var translatedTransform = new Transform(transform);
                translatedTransform.position.add(velocity.mul(delta, new Vector2d()));
                translatedCollider.refreshTranslatedVertices(translatedTransform);

                collectRelevantTiles(tileMapLayers,
                                     translatedTransform,
                                     translatedCollider,
                                     collisionTargets::add);
                collectRelevantEntities(resources.colliders,
                                        entity,
                                        collider.layer,
                                        collisionTargets::add,
                                        overlapTargets::add);

                moveWithCollision(resources.collisions,
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

        var iterations = MAX_ITERATIONS;
        final var collisions = new ArrayList<CollisionEventCandidate>();
        final var tmpTransform = new Transform(transform);

        while (distanceRemaining > 0 && iterations > 0) {
            collisions.clear();
            tmpTransform.set(transform);

            // Refresh non-translated vertices of the collider
            translatedCollider.refresh();

            final var distanceMoved = moveUntilCollision(
                    tmpTransform,
                    translatedTransform,
                    translatedCollider,
                    direction,
                    distanceRemaining,
                    collisionTargets,
                    overlapTargets,
                    (candidate, mode) -> collisions.add(new CollisionEventCandidate(mode, candidate))
            );

            for (final var collision : collisions) {
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
            --iterations;
        }
    }

    /**
     * Checking for collisions of fast-moving objects? Easy. Calculating push-out/penetration vectors for them? Easy.
     * Calculating the said vectors, preventing <i>"Bullet Through Paper"-problem</i> in the process? Hard.
     * <p>
     * The moving part itself is quite simple. "Stretch" the collider for the object being moved so that the collider is
     * the size of the whole translation. Now, if that stretched collider overlaps with anything solid, we know for sure
     * that moving will cause a collision. This is fast and cheap to check.
     * <p>
     * However, if we were to naively calculate a push-put vector for that stretched collider, chances are, the larger
     * half of the collider for fast-moving objects is on the other side of the wall we collided with. This causes EPA
     * to resolve shortest penetration vector out of the wrong side of the wall, effectively causing us to push the
     * entity through the wall. This is the <i>"Bullet Through Paper"-problem</i>.
     * <p>
     * That is easy to fix, too. Just split the moving operation to series of smaller steps with relatively small step
     * size, and as long as walls are thicker than the step size, bullets cannot phase through. The large issue here is
     * that when number of entities grows, the small increment in iterations might multiplicatively add up to huge
     * performance impact. However, by carefully choosing the step size and adjusting it dynamically, we may limit the
     * performance impact to situations where there are lot of simultaneous collisions *(which is arguably rare in
     * common use case)*
     * <p>
     * Then the next issue: Separate colliders near each other. If we calculate shortest push-out vector from an entity,
     * but it points towards another, we end up pushing the entity inside the latter entity! This allows things to move
     * inside walls, and as invalid collision responses are rapidly followed by new invalid responses, things start
     * phasing through walls again.
     * <p>
     * One way which I thought would fix all the issues listed here was to calculate the penetration vector against the
     * inverse direction of movement, which should then give vector out of the collider, towards the direction of entry.
     * Wrong. This blows up e.g. in cases where entity collides against the side of an obstacle, while still moving
     * faster vertically than horizontally. This then causes the collision normal direction to point to a vertical
     * direction, usually ending up pushing the entity inside a wall or otherwise snapping it jarringly around.
     * <p>
     * Now, what I actually ended up doing <i>(and what seems to be working surprisingly well)</i>, was to move using
     * dynamically adjusted step size and <strong>calculate an average of push-out vectors from all collisions</strong>.
     * Surprisingly, this seems to work as colliding <strong>exactly at the midpoint</strong> of two perfectly adjacent
     * colliders is quite improbable <i>(it does happen once in a while, resulting in bullets flying through walls, but
     * it's rare enough so fix is going to be on the backlog for a while)</i>.
     */
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
        var actualDistance = distance * 2.0;
        boolean wouldCollide;
        do {
            actualDistance /= 2.0;
            wouldCollide = collidesAfterMoving(actualDistance,
                                               direction,
                                               transform,
                                               translatedTransform,
                                               translatedCollider,
                                               collisionTargets);
        } while (wouldCollide && actualDistance > STEP_SIZE);

        final var collisions = collisionsAfterMoving(actualDistance,
                                                     direction,
                                                     transform,
                                                     translatedTransform,
                                                     translatedCollider,
                                                     collisionTargets);
        final double correctedDistance;
        final Vector2d correctedDirection;
        if (collisions.isEmpty()) {
            correctedDistance = actualDistance;
            correctedDirection = direction;
        } else {
            final var sum = new Vector2d(0.0);
            for (final ActualCollision collision : collisions) {
                final var result = collision.collision();
                final var translation = new Vector2d(direction).mul(actualDistance)
                                                               .sub(result.normal()
                                                                          .mul(result.depth(), new Vector2d()));
                sum.add(translation);
                collisionConsumer.accept(collision.candidate, Collision.Mode.COLLISION);
            }
            final var translation = sum.mul(1.0 / collisions.size());
            correctedDistance = translation.length();
            correctedDirection = translation.normalize();

        }

        moveDistanceTriggeringCollisions(transform,
                                         correctedDirection,
                                         correctedDistance,
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
            if (candidate.overlaps(translatedTransform, translatedCollider)) {
                collisionConsumer.accept(candidate, Collision.Mode.OVERLAP);
            }
        }

        final var translation = direction.mul(distanceToMove, new Vector2d());
        transform.position.add(translation);
    }

    private List<ActualCollision> collisionsAfterMoving(
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
        final var collisions = new ArrayList<ActualCollision>();
        for (final var target : collisionTargets) {
            final var collision = target.getCollision(translatedTransform, translatedCollider);
            if (collision.collides()) {
                collisions.add(new ActualCollision(target, collision));
            }
        }

        return collisions;
    }

    private boolean collidesAfterMoving(
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
            if (target.overlaps(translatedTransform, translatedCollider)) {
                return true;
            }
        }

        return false;
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

        public GJK2D.Result getCollision(final Transform transform, final Shape shape) {
            return GJK2D.getCollision(transform,
                                      shape,
                                      this.transform,
                                      this.entity != null
                                              ? this.entity.collider()
                                              : TILE_SHAPE);
        }

        public boolean overlaps(final Transform transform, final Shape shape) {
            return GJK2D.checkCollision(transform,
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
