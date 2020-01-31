package fi.jakojaannos.roguelite.engine.data.resources;

import lombok.Getter;
import lombok.experimental.Delegate;

import fi.jakojaannos.roguelite.engine.ecs.ProvidedResource;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

public class Time implements ProvidedResource, TimeManager {
    @Getter @Delegate private final TimeManager timeManager;

    public Time(final TimeManager timeManager) {
        this.timeManager = timeManager;
    }
}
