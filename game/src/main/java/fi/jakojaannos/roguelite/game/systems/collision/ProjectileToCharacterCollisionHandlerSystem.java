package fi.jakojaannos.roguelite.game.systems.collision;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.DamageInstance;
import fi.jakojaannos.roguelite.game.data.components.Health;
import fi.jakojaannos.roguelite.game.data.components.ProjectileStats;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import fi.jakojaannos.roguelite.game.data.resources.collision.Collisions;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.stream.Stream;

@Slf4j
public class ProjectileToCharacterCollisionHandlerSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.COLLISION_HANDLER)
                    .requireResource(Collisions.class)
                    .withComponent(RecentCollisionTag.class)
                    .withComponent(ProjectileStats.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world,
            final double delta
    ) {
        val entityManager = world.getEntityManager();
        val collisions = world.getResource(Collisions.class);

        entities.forEach(entity -> {
            val stats = entityManager.getComponentOf(entity, ProjectileStats.class).get();

            val entityCollisions = collisions.getEventsFor(entity)
                                             .stream()
                                             .map(CollisionEvent::getCollision)
                                             .filter(Collision::isEntity)
                                             .map(Collision::getAsEntityCollision);

            for (val collision : (Iterable<Collision.EntityCollision>) entityCollisions::iterator) {
                if (entityManager.hasComponent(collision.getOther(), Health.class)) {
                    val health = entityManager.getComponentOf(collision.getOther(), Health.class).get();
                    health.addDamageInstance(new DamageInstance(stats.damage));
                    entityManager.destroyEntity(entity);
                    // FIXME: Proper damage cool-down / invulnerability frame thingy
                    break;
                }
            }
        });
    }
}
