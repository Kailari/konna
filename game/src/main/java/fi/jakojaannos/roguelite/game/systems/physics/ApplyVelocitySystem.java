package fi.jakojaannos.roguelite.game.systems.physics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Math;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.game.GJK2D;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.resources.collision.Colliders;
import fi.jakojaannos.roguelite.game.data.resources.collision.Collisions;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;
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
@Slf4j
public class ApplyVelocitySystem implements ECSSystem {
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
    private final Vector2d tmpVelocity = new Vector2d();

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.PHYSICS_TICK)
                    .withComponent(Transform.class)
                    .withComponent(Velocity.class)
                    .requireResource(Collisions.class)
                    .requireResource(Colliders.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var entityManager = world.getEntityManager();
        final var entitiesWithCollider = world.getOrCreateResource(Colliders.class);
        final var collisionEvents = world.getOrCreateResource(Collisions.class);
        final var delta = world.getOrCreateResource(Time.class).getTimeStepInSeconds();

        final var tileMapLayers = getTileMapLayersWithCollision(world);

        final var collisionTargets = new ArrayList<CollisionCandidate>();
        final var overlapTargets = new ArrayList<CollisionCandidate>();
        entities.forEach(entity -> {
            collisionTargets.clear();
            overlapTargets.clear();
            final var transform = entityManager.getComponentOf(entity, Transform.class).orElseThrow();
            final var velocity = entityManager.getComponentOf(entity, Velocity.class).orElseThrow();

            if (velocity.length() < VELOCITY_EPSILON) {
                return;
            }

            if (entityManager.hasComponent(entity, Collider.class)) {
                final var collider = entityManager.getComponentOf(entity, Collider.class).orElseThrow();

                final var translatedCollider = new StretchedCollider(collider, transform);
                final var translatedTransform = new Transform(transform);
                translatedTransform.position.add(velocity.mul(delta, new Vector2d()));
                translatedCollider.refreshTranslatedVertices(translatedTransform);
                final var bounds = translatedCollider.getBounds(translatedTransform);

                collectRelevantTiles(tileMapLayers,
                                     translatedTransform,
                                     translatedCollider,
                                     collisionTargets::add,
                                     overlapTargets::add);
                entitiesWithCollider.collectRelevantEntities(entity,
                                                             collider.layer,
                                                             bounds,
                                                             collisionTargets::add,
                                                             overlapTargets::add);

                moveWithCollision(collisionEvents,
                                  world,
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
            final Consumer<CollisionCandidate> colliderConsumer,
            final Consumer<CollisionCandidate> overlapConsumer
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
                    if (tileType.isSolid()) {
                        colliderConsumer.accept(new CollisionCandidate(x, y));
                        break;
                    }
                }
            }
        }
    }

    private void moveWithCollision(
            final Collisions collisionEvents,
            final World world,
            final Entity entity,
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
                                   world,
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
        // Fail fast if we cannot move at all
        Optional<CollisionCandidate> collision;
        if ((collision = collisionsAfterMoving(STEP_SIZE,
                                               direction,
                                               transform,
                                               translatedTransform,
                                               translatedCollider,
                                               collisionTargets)).isPresent()
        ) {
            collision.ifPresent(candidate -> collisionConsumer.accept(candidate,
                                                                      Collision.Mode.COLLISION));
            moveDistanceTriggeringCollisions(transform,
                                             direction,
                                             0.0,
                                             translatedTransform,
                                             translatedCollider,
                                             overlapTargets,
                                             collisionConsumer);
            return 0.0;
        }

        // Return immediately if we can move the full distance
        if ((collision = collisionsAfterMoving(distance, direction, transform, translatedTransform,
                                               translatedCollider, collisionTargets)).isEmpty()) {
            moveDistanceTriggeringCollisions(transform,
                                             direction,
                                             distance,
                                             translatedTransform,
                                             translatedCollider,
                                             overlapTargets,
                                             collisionConsumer);
            return distance;
        }

        // Binary search for maximum steps we are allowed to take
        final var maxSteps = (int) (distance / STEP_SIZE);
        int stepsToTake = -1;
        for (int b = maxSteps; b >= 1; b /= 2) {
            // borderline case of full distance leading to collision and maxSteps not colliding
            while (stepsToTake <= maxSteps
                    && (collision = collisionsAfterMoving((stepsToTake + b) * STEP_SIZE,
                                                          direction,
                                                          transform,
                                                          translatedTransform,
                                                          translatedCollider,
                                                          collisionTargets)).isEmpty()
            ) {
                stepsToTake += b;
            }
        }

        // Just in case, needed for borderline cases
        if (stepsToTake > maxSteps) {
            stepsToTake = maxSteps;
        }

        if (stepsToTake == -1) {
            LOG.warn("Could not move. This should have been covered by early checks!");
            return 0.0;
        }

        final var distanceToMove = stepsToTake * STEP_SIZE;
        collision.ifPresent(candidate -> collisionConsumer.accept(candidate,
                                                                  Collision.Mode.COLLISION));
        moveDistanceTriggeringCollisions(transform,
                                         direction,
                                         distanceToMove,
                                         translatedTransform,
                                         translatedCollider,
                                         overlapTargets,
                                         collisionConsumer);

        return distanceToMove;
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

    private Optional<CollisionCandidate> collisionsAfterMoving(
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
                return Optional.of(target);
            }
        }

        return Optional.empty();
    }

    private void fireCollisionEvent(
            final Collisions collisions,
            final World world,
            final Entity entity,
            final CollisionCandidate candidate,
            final Collision.Mode mode
    ) {
        if (candidate.entity == null) {
            final var event = new CollisionEvent(Collision.tile(mode,
                                                                candidate.transform.position.x,
                                                                candidate.transform.position.y));
            collisions.fireCollisionEvent(entity, event);
        } else {
            final var event = new CollisionEvent(Collision.entity(mode, candidate.entity.entity));
            final var otherEvent = new CollisionEvent(Collision.entity(mode, entity));
            collisions.fireCollisionEvent(entity, event);
            collisions.fireCollisionEvent(candidate.entity.entity, otherEvent);

            world.getEntityManager()
                 .addComponentIfAbsent(candidate.entity.entity,
                                       RecentCollisionTag.class,
                                       RecentCollisionTag::new);
        }

        world.getEntityManager()
             .addComponentIfAbsent(entity, RecentCollisionTag.class, RecentCollisionTag::new);
    }

    private void moveWithoutCollision(
            final Transform transform,
            final Velocity velocity,
            final double delta
    ) {
        transform.position.add(velocity.mul(delta, this.tmpVelocity));
    }

    private List<TileMap<TileType>> getTileMapLayersWithCollision(final World world) {
        return world.getEntityManager()
                    .getEntitiesWith(TileMapLayer.class)
                    .map(EntityManager.EntityComponentPair::getComponent)
                    .filter(TileMapLayer::isCollisionEnabled)
                    .map(TileMapLayer::getTileMap)
                    .collect(Collectors.toList());
    }

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
            this.transform = entity.transform;
        }

        public boolean overlaps(final Transform transform, final Shape shape) {
            final var initialDirection = transform.position.sub(this.transform.position, new Vector2d());
            return GJK2D.intersects(transform,
                                    shape,
                                    this.transform,
                                    this.entity != null
                                            ? this.entity.collider
                                            : TILE_SHAPE,
                                    initialDirection);
        }
    }

    @RequiredArgsConstructor
    private static class CollisionEventCandidate {
        private final Collision.Mode mode;
        private final CollisionCandidate candidate;
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
