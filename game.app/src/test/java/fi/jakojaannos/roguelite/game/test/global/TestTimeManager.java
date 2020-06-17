package fi.jakojaannos.roguelite.game.test.global;

import fi.jakojaannos.riista.GameRunnerTimeManager;

public class TestTimeManager extends GameRunnerTimeManager {
    public TestTimeManager(final long timeStep) {
        super(timeStep);
    }

    public void setCurrentTick(final long tick) {
        this.currentTick = tick;
    }

    public void setCurrentTickAsSeconds(final double seconds) {
        this.currentTick = convertToTicks(seconds);
    }
}
