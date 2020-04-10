package fi.jakojaannos.roguelite.game.systems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.game.LogCategories;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;

public class HealthUpdateSystem implements ECSSystem {
    private static final Logger LOG = LoggerFactory.getLogger(HealthUpdateSystem.class);

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.LATE_TICK)
                    .requireResource(SessionStats.class)
                    .withComponent(Health.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var sessionStats = world.fetchResource(SessionStats.class);
        final var entityManager = world.getEntityManager();

        entities.forEach(entity -> {
            final var health = entityManager.getComponentOf(entity, Health.class).orElseThrow();

            final var damageInstances = health.damageInstances;
            for (final var instance : damageInstances) {
                health.currentHealth -= instance.damage();
                LOG.debug(LogCategories.HEALTH,
                          "Entity {} took {} damage. Has {} health remaining",
                          entity.getId(),
                          instance.damage(),
                          health.currentHealth);
            }

            if (health.currentHealth <= 0.0f) {
                LOG.trace(LogCategories.HEALTH,
                          "Entity {} health less than or equal to zero. Marking as dead.",
                          entity.getId());
                entityManager.addComponentIfAbsent(entity, DeadTag.class, DeadTag::new);

                for (final var instance : damageInstances) {
                    sessionStats.awardKillTo(instance.source());
                }
            }

            damageInstances.clear();
        });
    }
}

