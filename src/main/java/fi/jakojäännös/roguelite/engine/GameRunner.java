package fi.jakojäännös.roguelite.engine;

import fi.jakojäännös.roguelite.engine.input.InputEvent;
import fi.jakojäännös.roguelite.engine.input.InputProvider;
import fi.jakojäännös.roguelite.engine.view.GameRenderer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Optional;
import java.util.Queue;
import java.util.function.Supplier;

/**
 * Game simulation runner. Utility for running the game simulation.
 *
 * @param <TGame>
 * @param <TInput>
 */
@Slf4j
@RequiredArgsConstructor
public abstract class GameRunner<
        TGame extends Game<TState>,
        TInput extends InputProvider,
        TState>
        implements AutoCloseable {
    /**
     * Should the game loop continue running
     *
     * @param game game to check running status for
     *
     * @return <code>true</code> if runner should continue with the game loop. <code>false</code> to
     * break from the loop.
     */
    protected boolean shouldContinueLoop(@NonNull TGame game) {
        return !game.isFinished();
    }

    /**
     * Runs the game. The main entry-point for the game, the first and only call launcher should
     * need to make on the instance.
     *
     * @param game          Game to run
     * @param inputProvider Input provider for gathering input
     * @param renderer      Renderer to use for presenting the game. NOP-renderer is used if
     *                      provided renderer is <code>null</code>.
     */
    public void run(
            @NonNull Supplier<TState> defaultStateSupplier,
            @NonNull TGame game,
            @NonNull TInput inputProvider,
            GameRenderer<TState> renderer
    ) {
        if (game.isDisposed()) {
            throw new IllegalStateException("Tried running an already disposed game!");
        }

        // Create NOP-renderer if provided renderer is null and we are in the test environment
        GameRenderer<TState> actualRenderer =
                Optional.ofNullable(renderer)
                        .or(() -> Optional.ofNullable(System.getenv("ENVIRONMENT"))
                                          .filter(env -> env.equalsIgnoreCase("test"))
                                          .map(env -> new NOPRenderer()))
                        .orElseThrow(() -> new IllegalStateException("run called outside test environment without specifying a valid renderer!"));

        // Loop
        val state = defaultStateSupplier.get();
        game.getTime().refresh();
        var previousFrameTime = game.getTime().getCurrentRealTime();
        var accumulator = 0L;

        val simulationTimestep = 20L; // 50 TPS = 20ms per tick
        val simulationTimestepInSeconds = simulationTimestep / 1000.0;
        while (shouldContinueLoop(game)) {
            game.getTime().refresh();
            val currentFrameTime = game.getTime().getCurrentRealTime();
            var frameElapsedTime = currentFrameTime - previousFrameTime;
            if (frameElapsedTime > 250L) {
                LOG.warn("Last tick took over 250 ms! Slowing down simulation to catch up!");
                frameElapsedTime = 250L;
            }

            previousFrameTime = currentFrameTime;

            accumulator += frameElapsedTime;
            while (accumulator >= simulationTimestep) {
                simulateTick(state, game, inputProvider.pollEvents(), simulationTimestepInSeconds);

                game.getTime().progressGameTime(simulationTimestep);
                accumulator -= simulationTimestep;
            }

            val partialTickAlpha = accumulator / (double) simulationTimestep;
            presentGameState(state, actualRenderer, partialTickAlpha);
        }
    }

    /**
     * Simulates the game for a single tick.
     *
     * @param game        Game to simulate
     * @param inputEvents Input events to process during this tick
     * @param delta       Time since the last tick
     */
    public void simulateTick(
            TState state,
            TGame game,
            Queue<InputEvent> inputEvents,
            double delta
    ) {
        if (game.isDisposed()) {
            throw new IllegalStateException("Simulating tick for already disposed game!");
        }

        game.tick(state, inputEvents, delta);
    }

    /**
     * Presents the current game state to the user.
     *
     * @param state            Game state which to present
     * @param partialTickAlpha Time blending factor between the last two frames we should render at
     */
    public void presentGameState(
            TState state,
            GameRenderer<TState> renderer,
            double partialTickAlpha
    ) {
        renderer.render(state, partialTickAlpha);
    }

    private class NOPRenderer implements GameRenderer<TState> {
        @Override
        public void render(TState game, double partialTickAlpha) {
        }

        @Override
        public void close() {
        }
    }
}
