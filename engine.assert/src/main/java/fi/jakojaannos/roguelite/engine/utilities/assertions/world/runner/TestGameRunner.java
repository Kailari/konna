package fi.jakojaannos.roguelite.engine.utilities.assertions.world.runner;

import java.util.ArrayDeque;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameRunner;
import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.input.InputProvider;

public class TestGameRunner extends GameRunner {
    private final ArrayDeque<InputEvent> inputEvents = new ArrayDeque<>();
    private GameState state;

    public GameState getState() {
        return this.state;
    }

    public void setState(final GameState state) {
        this.state = state;
    }

    public TestGameRunner(final GameMode gameMode) {
        super();
        this.state = setActiveGameMode(gameMode);
        onModeChange(gameMode);
    }

    @Override
    protected boolean shouldContinueLoop() {
        return false;
    }

    @Override
    protected void onStateChange(final GameState state) {
        this.state = state;
    }

    @Override
    protected void onModeChange(final GameMode gameMode) {
    }

    @Override
    public void run(final GameMode defaultGameMode, final InputProvider inputProvider) {
        throw new UnsupportedOperationException("Running loop for TestGameRunner is not supported!");
    }

    void runForTicks(final long n) {
        this.state.world().commitEntityModifications();

        final var accumulator = new GameRunner.Accumulator();
        accumulator.accumulate(n * getTimeManager().getTimeStep());
        this.state = simulateFrame(this.state, accumulator, () -> this.inputEvents);
        this.inputEvents.clear();
    }
}
