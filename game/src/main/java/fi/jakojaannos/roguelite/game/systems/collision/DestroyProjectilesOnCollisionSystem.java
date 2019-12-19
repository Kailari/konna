package fi.jakojaannos.roguelite.game.systems.collision;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.ProjectileStats;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import fi.jakojaannos.roguelite.game.data.resources.collision.Collisions;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.stream.Stream;

@Slf4j
public class DestroyProjectilesOnCollisionSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.COLLISION_HANDLER)
                    .tickAfter(ProjectileToCharacterCollisionHandlerSystem.class)
                    .requireResource(Collisions.class)
                    .withComponent(ProjectileStats.class)
                    .withComponent(RecentCollisionTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val collisions = world.getOrCreateResource(Collisions.class);

        entities.forEach(entity -> {
            if (collisions.getEventsFor(entity)
                          .stream()
                          .map(CollisionEvent::getCollision)
                          .anyMatch(c -> c.getMode() == Collision.Mode.COLLISION)) {
                world.getEntityManager().destroyEntity(entity);
            }
        });
    }
}
