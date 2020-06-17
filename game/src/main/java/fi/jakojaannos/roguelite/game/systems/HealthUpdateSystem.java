package fi.jakojaannos.roguelite.game.systems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.LogCategories;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;

public class HealthUpdateSystem implements EcsSystem<HealthUpdateSystem.Resources, HealthUpdateSystem.EntityData, EcsSystem.NoEvents> {
    private static final Logger LOG = LoggerFactory.getLogger(HealthUpdateSystem.class);

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var sessionStats = resources.sessionStats;

        entities.forEach(entity -> {
            final var health = entity.getData().health;

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
                entity.addOrGet(DeadTag.class, DeadTag::new);

                for (final var instance : damageInstances) {
                    sessionStats.awardKillTo(instance.source());
                }
            }

            damageInstances.clear();
        });
    }

    public static record Resources(SessionStats sessionStats) {}

    public static record EntityData(Health health) {}
}

