package fi.jakojaannos.roguelite.engine.utilities.assertions.world.runner;

import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.SimulationInspector;

public class SimulationRunnerImpl implements SimulationInspector {
    private final TestGameRunner runner;

    public SimulationRunnerImpl(final TestGameRunner runner) {
        this.runner = runner;
    }

    @Override
    public SimulationInspector expect(final Consumer<GameState> expectation) {
        expectation.accept(this.runner.getState());
        return this;
    }

    @Override
    public SimulationInspector runsForTicks(final long n) {
        this.runner.runForTicks(n);
        return this;
    }

    @Override
    public SimulationInspector runsForSeconds(final double seconds) {
        final var ticks = this.runner.getTimeManager().convertToTicks(seconds);
        this.runner.runForTicks(ticks);
        return this;
    }
}
