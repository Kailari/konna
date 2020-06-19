package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.ecs.annotation.DisableOn;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.events.GameLostEvent;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;

public class UpdateSessionTimerSystem implements EcsSystem<UpdateSessionTimerSystem.Resources, EcsSystem.NoEntities, UpdateSessionTimerSystem.EventData> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<NoEntities>> entities,
            final EventData eventData
    ) {
        resources.sessionStats.endTimeStamp = resources.timeManager.getCurrentGameTime();
    }

    public static record Resources(
            TimeManager timeManager,
            SessionStats sessionStats
    ) {}

    public static record EventData(
            @DisableOn GameLostEvent gameLost
    ) {}
}
