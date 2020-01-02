package fi.jakojaannos.roguelite.game.test.global;

import fi.jakojaannos.roguelite.engine.utilities.UpdateableTimeManager;

public class TestTimeManager implements UpdateableTimeManager {
    private final long timestep;
    private final double timestepInSeconds;

    private long currentGameTime;

    public TestTimeManager(long timestepInMs) {
        this.timestep = timestepInMs;
        this.timestepInSeconds = timestepInMs / 1000.0;
    }

    public void setCurrentTick(final long tick) {
        this.currentGameTime = tick;
    }

    public void setCurrentTickAsSeconds(final double seconds) {
        this.currentGameTime = convertToTicks(seconds);
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
