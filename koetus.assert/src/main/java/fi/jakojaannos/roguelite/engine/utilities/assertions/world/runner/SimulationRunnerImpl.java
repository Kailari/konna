package fi.jakojaannos.roguelite.engine.utilities.assertions.world.runner;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import fi.jakojaannos.riista.GameRenderAdapter;
import fi.jakojaannos.riista.GameRunnerTimeManager;
import fi.jakojaannos.riista.application.GameTicker;
import fi.jakojaannos.riista.application.SimulationThread;
import fi.jakojaannos.riista.GameMode;
import fi.jakojaannos.riista.GameState;
import fi.jakojaannos.riista.input.InputEvent;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.PresentationInspector;

public class SimulationRunnerImpl<TPresentState> implements PresentationInspector<TPresentState> {
    private final GameTicker ticker;
    private final Queue<InputEvent> inputQueue = new ArrayDeque<>();
    private final SimulationThread simulationThread;

    private final GameRunnerTimeManager timeManager;

    private boolean terminateTriggered;

    @Override
    public boolean isTerminated() {
        return this.terminateTriggered;
    }

    public SimulationRunnerImpl(
            final GameMode gameMode,
            @Nullable final GameRenderAdapter<TPresentState> renderAdapter
    ) {
        this.timeManager = new GameRunnerTimeManager(20L);
        this.ticker = new GameTicker(this.timeManager,
                                     () -> {
                                         final var inputs = List.copyOf(this.inputQueue);
                                         this.inputQueue.clear();
                                         return inputs;
                                     },
                                     gameMode);

        this.simulationThread = new SimulationThread(this.ticker,
                                                     "simulation-runner",
                                                     this.timeManager,
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
    public PresentationInspector<TPresentState> receivesInput(final InputEvent inputEvent) {
        this.inputQueue.offer(inputEvent);
        return this;
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

    @Override
    public PresentationInspector<TPresentState> skipsTicks(final int n) {
        this.ticker.getState().world().commitEntityModifications();

        for (int i = 0; i < n; i++) {
            if (this.terminateTriggered) {
                break;
            }

            this.timeManager.nextTick();
        }

        return this;
    }
}
