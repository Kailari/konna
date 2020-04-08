package fi.jakojaannos.roguelite.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

import fi.jakojaannos.roguelite.engine.data.resources.Network;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.event.EventBus;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.input.InputProvider;
import fi.jakojaannos.roguelite.engine.network.NetworkManager;
import fi.jakojaannos.roguelite.engine.state.StateEvent;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

/**
 * Game simulation runner. Utility for running the main simulation loop.
 */
public abstract class GameRunner implements MainThread {
    private static final Logger LOG = LoggerFactory.getLogger(GameRunner.class);
    private final GameRunnerTimeManager timeManager;
    private final EventBus<StateEvent> stateBus;
    private final EventBus<InputEvent> inputBus;
    private final Events events;

    private final Object taskQueueLock = new Object();
    private final Queue<MainThreadTask> mainThreadTaskQueue = new ArrayDeque<>();

    private GameMode activeGameMode;
    private boolean running;

    protected long getMaxFrameTime() {
        return 250L;
    }

    protected long getFramerateLimit() {
        return -1L;
    }

    public Events getEvents() {
        return this.events;
    }

    protected GameRunner() {
        this.stateBus = new EventBus<>();
        this.inputBus = new EventBus<>();
        this.events = new Events(new EventBus<>(), this.inputBus, this.stateBus);
        this.timeManager = new GameRunnerTimeManager(20L);
    }

    @Override
    public void queueTask(final MainThreadTask task) {
        synchronized (this.taskQueueLock) {
            this.mainThreadTaskQueue.offer(task);
        }
    }

    /**
     * Runs the game. The main entry-point for the game, the first and only call launcher should need to make on the
     * instance.
     *
     * @param defaultGameMode Game mode to run
     * @param inputProvider   Input provider for gathering input
     */
    public void run(
            final GameMode defaultGameMode,
            final InputProvider inputProvider
    ) {
        LOG.info("Runner starting...");

        // Loop
        final var accumulator = new Accumulator();
        final var initialTime = System.currentTimeMillis();
        var previousFrameTime = initialTime;
        var frames = 0;

        LOG.info("Entering main loop");
        this.activeGameMode = defaultGameMode;
        var state = createStateForActiveGameMode();
        onModeChange(this.activeGameMode);
        try {
            this.running = true;
            while (this.running && shouldContinueLoop()) {
                final var currentFrameTime = System.currentTimeMillis();
                final var frameElapsedTime = getTimeSinceLastTick(previousFrameTime, currentFrameTime);
                previousFrameTime = currentFrameTime;
                accumulator.accumulate(frameElapsedTime);

                state = simulateFrame(state, accumulator, inputProvider);
                frames++;

                limitFramerate();
            }
        } finally {
            if (this.activeGameMode != null) {
                try {
                    this.activeGameMode.close();
                } catch (final Exception e) {
                    LOG.warn("Cleaning up the game mode failed:", e);
                }
            }
        }

        final var totalTime = System.currentTimeMillis() - initialTime;
        final var totalTimeSeconds = totalTime / 1000.0;

        final var avgTimePerTick = totalTime / (double) this.timeManager.getCurrentGameTime();
        final var avgTicksPerSecond = this.timeManager.getCurrentGameTime() / totalTimeSeconds;

        final var avgTimePerFrame = totalTime / (double) frames;
        final var avgFramesPerSecond = frames / totalTimeSeconds;
        LOG.info("Finished execution after {} seconds", totalTimeSeconds);
        LOG.info("\tTicks:\t\t{}", this.timeManager.getCurrentGameTime());
        LOG.info("\tAvg. TPT:\t{}", avgTimePerTick);
        LOG.info("\tAvg. TPS:\t{}", avgTicksPerSecond);
        LOG.info("\tFrames:\t\t{}", frames);
        LOG.info("\tAvg. TPF:\t{}", avgTimePerFrame);
        LOG.info("\tAvg. FPS:\t{}", avgFramesPerSecond);
    }

    protected GameState simulateFrame(
            final GameState state,
            final Accumulator accumulator,
            final InputProvider inputProvider
    ) {
        synchronized (this.taskQueueLock) {
            while (!this.mainThreadTaskQueue.isEmpty()) {
                this.mainThreadTaskQueue.poll().execute(state);
            }
        }

        var stateHasChanged = false;
        var modeHasChanged = false;
        var activeState = state;
        while (accumulator.canSimulateTick(this.timeManager.getTimeStep())) {
            pollInputEvents(inputProvider);

            this.activeGameMode.systemDispatcher().tick(state.world());
            accumulator.nextTick(this.timeManager.getTimeStep());
            this.timeManager.nextTick();

            while (this.stateBus.hasEvents()) {
                final var stateEvent = this.stateBus.pollEvent();
                if (stateEvent instanceof StateEvent.ChangeState changeState) {
                    activeState = changeState.gameState();
                    stateHasChanged = true;
                } else if (stateEvent instanceof StateEvent.ChangeMode changeMode) {
                    if (this.activeGameMode != null) {
                        try {
                            this.activeGameMode.close();
                        } catch (final Exception e) {
                            LOG.error("Error while cleaning up old game mode: " + e.getMessage());
                        }
                    }
                    this.activeGameMode = changeMode.gameMode();
                    activeState = createStateForActiveGameMode();
                    modeHasChanged = true;
                } else if (stateEvent instanceof StateEvent.Shutdown) {
                    this.running = false;
                }
            }
        }

        if (modeHasChanged) {
            onModeChange(this.activeGameMode);
        }

        if (stateHasChanged) {
            onStateChange(state);
        }

        return activeState;
    }

    protected abstract boolean shouldContinueLoop();

    protected abstract void onStateChange(final GameState state);

    protected abstract void onModeChange(final GameMode gameMode);

    protected long getTimeSinceLastTick(final long previousFrameTime, final long currentFrameTime) {
        var frameElapsedTime = currentFrameTime - previousFrameTime;
        if (frameElapsedTime > getMaxFrameTime()) {
            LOG.warn("Last tick took over 250 ms! Slowing down simulation to catch up!");
            frameElapsedTime = getMaxFrameTime();
        }
        return frameElapsedTime;
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

    private GameState createStateForActiveGameMode() {
        final var world = World.createNew();
        world.registerResource(Events.class, this.events);
        world.registerResource(TimeManager.class, this.timeManager);
        world.registerResource(MainThread.class, this);

        // FIXME: Get rid of this
        world.registerResource(Time.class, new Time(this.timeManager));

        // FIXME: Figure out something smarter
        world.registerResource(Network.class, new Network() {
            @Override
            public Optional<NetworkManager<?>> getNetworkManager() {
                return Optional.empty();
            }

            @Override
            public Optional<String> getConnectionError() {
                return Optional.empty();
            }

            @Override
            public void setConnectionError(final String error) {
            }
        });
        return this.activeGameMode.stateFactory().apply(world);
    }

    private void pollInputEvents(final InputProvider inputProvider) {
        inputProvider.pollEvents().forEach(this.inputBus::fire);
    }

    public static final class Accumulator {
        private long value;

        public void accumulate(final long amount) {
            this.value += amount;
        }

        public void nextTick(final long timeStep) {
            this.value -= timeStep;
        }

        public boolean canSimulateTick(final long timeStep) {
            return this.value > timeStep;
        }

        public long get() {
            return this.value;
        }
    }
}
