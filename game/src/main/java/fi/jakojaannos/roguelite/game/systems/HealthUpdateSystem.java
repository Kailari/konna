package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.LogCategories;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.resources.Kills;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.stream.Stream;

@Slf4j

public class HealthUpdateSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.LATE_TICK)
                    .requireResource(Kills.class)
                    .withComponent(Health.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val kills = world.getOrCreateResource(Kills.class);
        val entityManager = world.getEntityManager();

        entities.forEach(entity -> {
            val health = entityManager.getComponentOf(entity, Health.class).orElseThrow();

            val damageInstances = health.damageInstances;
            for (val instance : damageInstances) {
                health.currentHealth -= instance.damage;
                LOG.debug(LogCategories.HEALTH, "Entity {} took {} damage. Has {} health remaining", entity.getId(), instance.damage, health.currentHealth);
            }

            damageInstances.clear();

            if (health.currentHealth <= 0.0f) {
                LOG.debug(LogCategories.HEALTH, "Entity {} health less than or equal to zero. Marking as dead.", entity.getId());
                entityManager.addComponentIfAbsent(entity, DeadTag.class, DeadTag::new);

                for (val instance : damageInstances) {
                    kills.awardKillTo(instance.source);
                }
            }
        });
    }
}

