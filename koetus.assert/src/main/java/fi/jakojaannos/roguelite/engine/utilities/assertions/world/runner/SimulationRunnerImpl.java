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
    private final GameRunnerTimeManager timeManager;

    private boolean terminateTriggered;

    @Override
    public boolean isTerminated() {
        return this.terminateTriggered;
    }

    public SimulationRunnerImpl(final GameMode gameMode) {
        this.timeManager = new GameRunnerTimeManager(20L);
        this.ticker = new GameTicker(this.timeManager, () -> this.inputQueue, gameMode);
    }

    @Override
    public GameState state() {
        return this.ticker.getState();
    }

    @Override
    public GameMode mode() {
        return this.ticker.getMode();
    }

    @Override
    public Queue<InputEvent> inputQueue() {
        return this.inputQueue;
    }

    @Override
    public SimulationInspector expect(final Consumer<GameState> expectation) {
        expectation.accept(this.ticker.getState());
        return this;
    }

    @Override
    public SimulationInspector runsForTicks(final long n) {
        this.ticker.getState().world().commitEntityModifications();

        for (int i = 0; i < n; i++) {
            if (this.terminateTriggered) {
                break;
            }

            this.ticker.simulateTick(() -> this.terminateTriggered = true);
            this.timeManager.nextTick();
        }

        return this;
    }

    @Override
    public SimulationInspector runsForSeconds(final double seconds) {
        final var ticks = this.ticker.getTimeManager().convertToTicks(seconds);
        return runsForTicks(ticks);
    }
}
