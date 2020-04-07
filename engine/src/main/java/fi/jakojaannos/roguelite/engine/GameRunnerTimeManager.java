package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

public final class GameRunnerTimeManager implements TimeManager {
    private final long timeStep;
    private final double timeStepInSeconds;

    private long currentTick;

    @Override
    public long getTimeStep() {
        return this.timeStep;
    }

    @Override
    public double getTimeStepInSeconds() {
        return this.timeStepInSeconds;
    }

    @Override
    public long getCurrentGameTime() {
        return this.currentTick;
    }

    public GameRunnerTimeManager(final long timeStep) {
        this.timeStep = timeStep;
        this.timeStepInSeconds = timeStep / 1000.0;
    }

    public void nextTick() {
        this.currentTick++;
    }
}
