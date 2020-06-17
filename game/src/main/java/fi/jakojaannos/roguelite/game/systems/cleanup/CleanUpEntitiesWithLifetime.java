package fi.jakojaannos.roguelite.game.systems.cleanup;

import java.util.function.Predicate;
import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.Lifetime;

public class CleanUpEntitiesWithLifetime implements EcsSystem<CleanUpEntitiesWithLifetime.Resources, CleanUpEntitiesWithLifetime.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        entities.filter(onlyExpired(resources.timeManager))
                .forEach(EntityDataHandle::destroy);
    }

    private Predicate<EntityDataHandle<EntityData>> onlyExpired(final TimeManager timeManager) {
        return entity -> {
            final var currentTick = timeManager.getCurrentGameTime();
            final var remaining = lifetimeRemainingOn(currentTick, entity.getData().lifetime);
            return remaining <= 0;
        };
    }

    private static long lifetimeRemainingOn(final long tick, final Lifetime lifetime) {
        return (lifetime.timestamp + lifetime.duration) - tick;
    }

    public static record Resources(TimeManager timeManager) {}

    public static record EntityData(Lifetime lifetime) {}
}
