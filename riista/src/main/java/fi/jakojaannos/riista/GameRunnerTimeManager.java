package fi.jakojaannos.riista;

import fi.jakojaannos.riista.utilities.TimeManager;

public class GameRunnerTimeManager implements TimeManager {
    private final long timeStep;
    private final double timeStepInSeconds;

    protected long currentTick;

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

    public void setCurrentTick(final long tick) {
        this.currentTick = tick;
    }

    public GameRunnerTimeManager(final long timeStep) {
        this.timeStep = timeStep;
        this.timeStepInSeconds = timeStep / 1000.0;
    }

    public void nextTick() {
        this.currentTick++;
    }
}
