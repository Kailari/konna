package fi.jakojaannos.roguelite.engine.utilities;

public class SimpleTimeManager implements UpdateableTimeManager {
    private final long timestep;
    private final double timestepInSeconds;

    private long currentGameTime;

    public SimpleTimeManager(final long timestepInMs) {
        this.timestep = timestepInMs;
        this.timestepInSeconds = timestepInMs / 1000.0;
    }

    @Override
    public long getTimeStep() {
        return this.timestep;
    }

    @Override
    public double getTimeStepInSeconds() {
        return this.timestepInSeconds;
    }

    @Override
    public long getCurrentGameTime() {
        return this.currentGameTime;
    }

    @Override
    public void refresh() {
        ++this.currentGameTime;
    }
}
