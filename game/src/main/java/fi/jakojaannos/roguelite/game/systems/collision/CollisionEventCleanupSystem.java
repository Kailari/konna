package fi.jakojaannos.roguelite.game.systems.collision;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import fi.jakojaannos.roguelite.game.data.resources.collision.Collisions;
import fi.jakojaannos.roguelite.game.systems.physics.ApplyVelocitySystem;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

/**
 * Performs cleanup on all {@link Collider Colliders} to clear all unprocessed {@link CollisionEvent CollisionEvents} at
 * the end of each tick.
 *
 * @see ApplyVelocitySystem
 */
public class CollisionEventCleanupSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CLEANUP)
                    .requireResource(Collisions.class)
                    .withComponent(RecentCollisionTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        world.getOrCreateResource(Collisions.class).clear();
        entities.forEach(entity -> world.getEntityManager().removeComponentFrom(entity, RecentCollisionTag.class));
    }
}
