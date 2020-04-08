package fi.jakojaannos.roguelite.game.systems.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.Lifetime;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

public class CleanUpEntitiesWithLifetime implements ECSSystem {
    private static final Logger LOG = LoggerFactory.getLogger(CleanUpEntitiesWithLifetime.class);

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CLEANUP)
                    .requireProvidedResource(TimeManager.class)
                    .withComponent(Lifetime.class)
                    .tickBefore(ReaperSystem.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var entityManager = world.getEntityManager();
        final var timeManager = world.fetchResource(TimeManager.class);

        entities.filter(onlyExpired(entityManager, timeManager))
                .forEach(entityManager::destroyEntity);
    }

    private Predicate<Entity> onlyExpired(final EntityManager entityManager, final TimeManager timeManager) {
        final var currentTick = timeManager.getCurrentGameTime();
        return entity -> entityManager.getComponentOf(entity, Lifetime.class)
                                      .map(toLifetimeRemainingOn(currentTick))
                                      .map(remainingLifetime -> remainingLifetime <= 0)
                                      .orElse(false);
    }

    private static Function<Lifetime, Long> toLifetimeRemainingOn(final long tick) {
        return lifetime -> (lifetime.timestamp + lifetime.duration) - tick;
    }
}
