package fi.jakojaannos.roguelite.game.systems.collision;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.DamageInstance;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.game.data.resources.collision.Collisions;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.stream.Stream;

@Slf4j
public class EnemyToPlayerCollisionHandlerSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.COLLISION_HANDLER)
                    .requireResource(Collisions.class)
                    .withComponent(RecentCollisionTag.class)
                    .withComponent(PlayerTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val timeManager = world.getOrCreateResource(Time.class);
        val entityManager = world.getEntityManager();
        val collisions = world.getOrCreateResource(Collisions.class);

        entities.forEach(entity -> {
            val health = entityManager.getComponentOf(entity, Health.class).get();

            val entityCollisions = collisions.getEventsFor(entity)
                                             .stream()
                                             .map(CollisionEvent::getCollision)
                                             .filter(Collision::isEntity)
                                             .map(Collision::getAsEntityCollision);

            for (val collision : (Iterable<Collision.EntityCollision>) entityCollisions::iterator) {
                val other = collision.getOther();
                if (entityManager.hasComponent(other, EnemyTag.class)
                        && entityManager.hasComponent(other, EnemyMeleeWeaponStats.class)
                        && entityManager.hasComponent(other, CharacterAbilities.class)
                ) {
                    val abilities = entityManager.getComponentOf(other, CharacterAbilities.class).get();
                    val stats = entityManager.getComponentOf(other, EnemyMeleeWeaponStats.class).get();

                    if (abilities.attackTimer < stats.attackRate) {
                        continue;
                    }

                    health.addDamageInstance(new DamageInstance(stats.damage), timeManager.getCurrentGameTime());
                    abilities.attackTimer = 0.0;
                }
            }
        });
    }
}
