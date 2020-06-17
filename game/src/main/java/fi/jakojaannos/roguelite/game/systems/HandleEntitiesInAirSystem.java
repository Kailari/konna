package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.InAir;

public class HandleEntitiesInAirSystem implements EcsSystem<HandleEntitiesInAirSystem.Resources, HandleEntitiesInAirSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var timeManager = resources.timeManager;

        entities.filter(entity -> airtimeEnding(timeManager.getCurrentGameTime(), entity))
                .forEach(entity -> entity.removeComponent(InAir.class));
    }

    private boolean airtimeEnding(final long currentGameTime, final EntityDataHandle<EntityData> entity) {
        final var inAir = entity.getData().inAir;
        return currentGameTime > inAir.flightStartTimeStamp + inAir.flightDuration;
    }

    public static record Resources(TimeManager timeManager) {}

    public static record EntityData(InAir inAir) {}
}
