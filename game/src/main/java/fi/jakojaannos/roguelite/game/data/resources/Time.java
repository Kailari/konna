package fi.jakojaannos.roguelite.game.data.resources;

import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import lombok.experimental.Delegate;

public class Time implements Resource, TimeManager {
    @Delegate private TimeManager timeManager;

    public void setTimeManager(final TimeManager timeManager) {
        this.timeManager = timeManager;
    }
}
