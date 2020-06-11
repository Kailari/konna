package fi.jakojaannos.roguelite.engine.utilities.assertions.world.runner;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

import fi.jakojaannos.riista.application.GameTicker;
import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameRunnerTimeManager;
import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.SimulationInspector;

public class SimulationRunnerImpl implements SimulationInspector {
    private final GameTicker ticker;
    private final Queue<InputEvent> inputQueue = new ArrayDeque<>();

    private boolean terminateTriggered;

    public SimulationRunnerImpl(final GameMode gameMode) {
        final var timeManager = new GameRunnerTimeManager(20L);
        this.ticker = new GameTicker(timeManager, () -> this.inputQueue, gameMode);
    }

    @Override
    public SimulationInspector expect(final Consumer<GameState> expectation) {
        expectation.accept(this.ticker.getState());
        return this;
    }

    @Override
    public SimulationInspector runsForTicks(final long n) {
        this.ticker.getState().world().commitEntityModifications();

        this.terminateTriggered = false;
        for (int i = 0; i < n; i++) {
            this.ticker.simulateTick(() -> this.terminateTriggered = true);

            if (this.terminateTriggered) {
                break;
            }
        }

        return this;
    }

    @Override
    public SimulationInspector runsForSeconds(final double seconds) {
        final var ticks = this.ticker.getTimeManager().convertToTicks(seconds);
        return runsForTicks(ticks);
    }
}
