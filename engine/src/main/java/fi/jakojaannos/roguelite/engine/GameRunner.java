package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.input.InputProvider;
import fi.jakojaannos.roguelite.engine.state.GameState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
        TGame extends Game,
        TInput extends InputProvider>
        implements AutoCloseable {
    /**
     * Should the game loop continue running
     *
     * @param game game to check running status for
     *
     * @return <code>true</code> if runner should continue with the game loop. <code>false</code> to
     * break from the loop.
     */
    protected boolean shouldContinueLoop(TGame game) {
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
            final Supplier<GameState> defaultStateSupplier,
            final TGame game,
            final TInput inputProvider,
            final RendererFunction renderer
    ) {
        if (game.isDisposed()) {
            throw new IllegalStateException("Tried running an already disposed game!");
        }

        LOG.info("Runner starting...");

        // Loop
        var state = defaultStateSupplier.get();
        val initialTime = System.currentTimeMillis();
        var previousFrameTime = initialTime;
        var accumulator = 0L;
        var ticks = 0;
        var frames = 0;

        LOG.info("Entering main loop");
        val events = new Events();
        while (shouldContinueLoop(game)) {
            val currentFrameTime = System.currentTimeMillis();
            var frameElapsedTime = currentFrameTime - previousFrameTime;
            if (frameElapsedTime > 250L) {
                LOG.warn("Last tick took over 250 ms! Slowing down simulation to catch up!");
                frameElapsedTime = 250L;
            }

            previousFrameTime = currentFrameTime;

            accumulator += frameElapsedTime;
            while (accumulator >= game.getTime().getTimeStep()) {
                inputProvider.pollEvents()
                             .forEach(events.getInput()::fire);

                state = simulateTick(state, game, events);
                accumulator -= game.getTime().getTimeStep();
                ++ticks;
            }

            val partialTickAlpha = accumulator / (double) game.getTime().getTimeStep();
            renderer.render(state, partialTickAlpha, events);
            presentGameState(state, renderer, partialTickAlpha, events);
            frames++;
        }

        val totalTime = System.currentTimeMillis() - initialTime;
        val totalTimeSeconds = totalTime / 1000.0;

        val avgTimePerTick = totalTime / (double) ticks;
        val avgTicksPerSecond = ticks / totalTimeSeconds;

        val avgTimePerFrame = totalTime / (double) frames;
        val avgFramesPerSecond = frames / totalTimeSeconds;
        LOG.info("Finished execution after {} seconds", totalTimeSeconds);
        LOG.info("\tTicks:\t\t{}", ticks);
        LOG.info("\tAvg. TPT:\t{}", avgTimePerTick);
        LOG.info("\tAvg. TPS:\t{}", avgTicksPerSecond);
        LOG.info("\tFrames:\t\t{}", frames);
        LOG.info("\tAvg. TPF:\t{}", avgTimePerFrame);
        LOG.info("\tAvg. FPS:\t{}", avgFramesPerSecond);
    }

    /**
     * Simulates the game for a single tick.
     *
     * @param game   Game to simulate
     * @param events Events to process during this tick
     */
    public GameState simulateTick(
            final GameState state,
            final TGame game,
            final Events events
    ) {
        if (game.isDisposed()) {
            throw new IllegalStateException("Simulating tick for already disposed game!");
        }

        val newState = game.tick(state, events);
        game.updateTime();
        return newState;
    }

    /**
     * Presents the current game state to the user.
     *
     * @param state            Game state which to present
     * @param partialTickAlpha Time blending factor between the last two frames we should render at
     */
    public void presentGameState(
            final GameState state,
            final RendererFunction renderer,
            final double partialTickAlpha,
            final Events events
    ) {
        renderer.render(state, partialTickAlpha, events);
    }

    public interface RendererFunction {
        void render(GameState state, double partialTickAlpha, Events events);
    }
}
