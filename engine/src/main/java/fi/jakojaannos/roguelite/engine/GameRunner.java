package fi.jakojaannos.roguelite.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.input.InputProvider;
import fi.jakojaannos.roguelite.engine.state.GameState;

/**
 * Game simulation runner. Utility for running the game simulation.
 *
 * @param <TGame> Type of the game to run
 */
public class GameRunner<TGame extends Game> implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(GameRunner.class);

    protected long getMaxFrameTime() {
        return 250L;
    }

    protected long getFramerateLimit() {
        return -1L;
    }

    /**
     * Should the game loop continue running
     *
     * @param game game to check running status for
     *
     * @return <code>true</code> if runner should continue with the game loop. <code>false</code> to
     *         break from the loop.
     */
    protected boolean shouldContinueLoop(final TGame game) {
        return !game.isFinished();
    }

    /**
     * Runs the game. The main entry-point for the game, the first and only call launcher should need to make on the
     * instance.
     *
     * @param game          Game to run
     * @param inputProvider Input provider for gathering input
     * @param renderer      Renderer to use for presenting the game. NOP-renderer is used if provided renderer is
     *                      <code>null</code>.
     */
    public void run(
            final Supplier<GameState> defaultStateSupplier,
            final TGame game,
            final InputProvider inputProvider,
            final RendererFunction renderer
    ) {
        if (game.isDisposed()) {
            throw new IllegalStateException("Tried running an already disposed game!");
        }

        LOG.info("Runner starting...");

        // Loop
        var state = defaultStateSupplier.get();
        state.getWorld().getEntityManager().applyModifications();
        final var initialTime = System.currentTimeMillis();
        var previousFrameTime = initialTime;
        var accumulator = 0L;
        var ticks = 0;
        var frames = 0;

        LOG.info("Entering main loop");
        final var events = new Events();
        onStateChange(state, events, game);
        try {
            while (shouldContinueLoop(game)) {
                final var currentFrameTime = System.currentTimeMillis();
                var frameElapsedTime = currentFrameTime - previousFrameTime;
                if (frameElapsedTime > getMaxFrameTime()) {
                    LOG.warn("Last tick took over 250 ms! Slowing down simulation to catch up!");
                    frameElapsedTime = getMaxFrameTime();
                }

                previousFrameTime = currentFrameTime;

                accumulator += frameElapsedTime;
                while (accumulator >= game.getTime().getTimeStep()) {
                    inputProvider.pollEvents()
                                 .forEach(events.input()::fire);

                    final var oldState = state;
                    state = simulateTick(state, game, events);
                    if (oldState != state) {
                        onStateChange(state, events, game);
                    }
                    accumulator -= game.getTime().getTimeStep();
                    ++ticks;
                }

                final var partialTickAlpha = accumulator / (double) game.getTime().getTimeStep();
                presentGameState(state, renderer, partialTickAlpha, events);
                frames++;

                limitFramerate();
            }
        } finally {
            try {
                state.close();
            } catch (final Exception e) {
                LOG.warn("Destroying the game state failed:", e);
            }
        }
        final var totalTime = System.currentTimeMillis() - initialTime;
        final var totalTimeSeconds = totalTime / 1000.0;

        final var avgTimePerTick = totalTime / (double) ticks;
        final var avgTicksPerSecond = ticks / totalTimeSeconds;

        final var avgTimePerFrame = totalTime / (double) frames;
        final var avgFramesPerSecond = frames / totalTimeSeconds;
        LOG.info("Finished execution after {} seconds", totalTimeSeconds);
        LOG.info("\tTicks:\t\t{}", ticks);
        LOG.info("\tAvg. TPT:\t{}", avgTimePerTick);
        LOG.info("\tAvg. TPS:\t{}", avgTicksPerSecond);
        LOG.info("\tFrames:\t\t{}", frames);
        LOG.info("\tAvg. TPF:\t{}", avgTimePerFrame);
        LOG.info("\tAvg. FPS:\t{}", avgFramesPerSecond);
    }

    private void onStateChange(final GameState state, final Events events, final TGame game) {
        state.getWorld().registerResource(events);
        state.getWorld().registerResource(Time.class, new Time(game.getTime()));
        state.getWorld().registerResource(MainThread.class, game);
    }

    private void limitFramerate() {
        if (getFramerateLimit() > 0) {
            final var targetTimePerFrame = 1.0 / getFramerateLimit();
            final var remaining = (long) (1000L * targetTimePerFrame * 0.95);
            try {
                Thread.sleep(remaining);
            } catch (final InterruptedException ignored) {
            }
        }
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

        final var newState = game.tick(state, events);
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

    @Override
    @SuppressWarnings("RedundantThrows")
    public void close() throws Exception {
    }

    public interface RendererFunction {
        void render(GameState state, double partialTickAlpha, Events events);
    }
}
