package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.events.HordeEndEvent;
import fi.jakojaannos.roguelite.game.data.events.HordeStartEvent;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;

public class HordeControllerSystem implements EcsSystem<HordeControllerSystem.Resources, EcsSystem.NoEntities, EcsSystem.NoEvents> {
    private final long initialCalm;
    private final long calmDuration;
    private final long hordeTimePeriod;

    public HordeControllerSystem(final long initialCalm, final long calmDuration, final long hordeDuration) {
        this.initialCalm = initialCalm;
        this.calmDuration = calmDuration;
        this.hordeTimePeriod = hordeDuration + calmDuration;
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<NoEntities>> entities,
            final NoEvents noEvents
    ) {
        final var currentTime = resources.timeManager.getCurrentGameTime() - resources.sessionStats.beginTimeStamp;
        if (currentTime < this.initialCalm) {
            return;
        }

        final var relativeTime = (currentTime - this.initialCalm) % this.hordeTimePeriod;
        if (relativeTime == 0) {
            resources.events.system().fire(new HordeStartEvent());
        } else if (relativeTime == this.calmDuration) {
            resources.events.system().fire(new HordeEndEvent());
        }
    }

    public static record Resources(
            TimeManager timeManager,
            SessionStats sessionStats,
            Events events
    ) {}
}
