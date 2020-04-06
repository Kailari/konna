package fi.jakojaannos.roguelite.engine.data.resources;

import fi.jakojaannos.roguelite.engine.ecs.legacy.ProvidedResource;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

public record Time(TimeManager timeManager) implements ProvidedResource, TimeManager {
    @Override
    public long getTimeStep() {
        return this.timeManager.getTimeStep();
    }

    @Override
    public double getTimeStepInSeconds() {
        return this.timeManager.getTimeStepInSeconds();
    }

    @Override
    public long getCurrentGameTime() {
        return this.timeManager.getCurrentGameTime();
    }

    @Override
    public double getTicksPerSecond() {
        return this.timeManager.getTicksPerSecond();
    }

    @Override
    public long convertToTicks(final double seconds) {
        return this.timeManager.convertToTicks(seconds);
    }

    @Override
    public double convertToSeconds(final long ticks) {
        return this.timeManager.convertToSeconds(ticks);
    }
}
