package fi.jakojaannos.roguelite.engine.utilities.assertions.world.runner;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import fi.jakojaannos.riista.GameRenderAdapter;
import fi.jakojaannos.riista.application.GameTicker;
import fi.jakojaannos.riista.application.SimulationThread;
import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameRunnerTimeManager;
import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.PresentationInspector;

public class SimulationRunnerImpl<TPresentState> implements PresentationInspector<TPresentState> {
    private final GameTicker ticker;
    private final Queue<InputEvent> inputQueue = new ArrayDeque<>();
    private final SimulationThread simulationThread;

    private boolean terminateTriggered;

    @Override
    public boolean isTerminated() {
        return this.terminateTriggered;
    }

    public SimulationRunnerImpl(
            final GameMode gameMode,
            @Nullable final GameRenderAdapter<TPresentState> renderAdapter
    ) {
        final var timeManager = new GameRunnerTimeManager(20L);
        this.ticker = new GameTicker(timeManager, () -> this.inputQueue, gameMode);

        this.simulationThread = new SimulationThread(this.ticker,
                                                     "simulation-runner",
                                                     timeManager,
                                                     () -> this.terminateTriggered = true,
                                                     renderAdapter);
        if (renderAdapter != null) {
            renderAdapter.onGameModeChange(gameMode, this.ticker.getState());
        }
        this.ticker.getState().world().commitEntityModifications();
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
    public PresentationInspector<TPresentState> expect(final Consumer<GameState> expectation) {
        expectation.accept(this.ticker.getState());
        return this;
    }

    @Override
    public PresentationInspector<TPresentState> runsForTicks(final long n) {
        this.ticker.getState().world().commitEntityModifications();

        for (int i = 0; i < n; i++) {
            if (this.terminateTriggered) {
                break;
            }

            this.simulationThread.tick();
        }

        return this;
    }

    @Override
    public PresentationInspector<TPresentState> runsForSeconds(final double seconds) {
        final var ticks = this.ticker.getTimeManager().convertToTicks(seconds);
        return runsForTicks(ticks);
    }
}
