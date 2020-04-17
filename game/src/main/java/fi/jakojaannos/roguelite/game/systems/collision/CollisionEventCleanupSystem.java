package fi.jakojaannos.roguelite.game.systems.collision;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import fi.jakojaannos.roguelite.game.data.resources.collision.Collisions;
import fi.jakojaannos.roguelite.game.systems.physics.ApplyVelocitySystem;

/**
 * Performs cleanup on all {@link Collider Colliders} to clear all unprocessed {@link CollisionEvent CollisionEvents} at
 * the end of each tick.
 *
 * @see ApplyVelocitySystem
 */
public class CollisionEventCleanupSystem implements EcsSystem<CollisionEventCleanupSystem.Resources, CollisionEventCleanupSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        resources.collisions.clear();
        entities.forEach(entity -> entity.removeComponent(RecentCollisionTag.class));
    }

    public static record Resources(Collisions collisions) {}

    public static record EntityData(RecentCollisionTag tag) {}
}
