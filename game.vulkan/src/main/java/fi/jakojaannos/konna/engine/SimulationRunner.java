package fi.jakojaannos.konna.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameRunnerTimeManager;
import fi.jakojaannos.roguelite.engine.input.InputProvider;

public class SimulationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(SimulationRunner.class);

    private final ScheduledExecutorService executor;
    private final GameRunnerTimeManager timeManager;
    private final PresentableStateQueue stateQueue;
    private final Runnable onTerminate;

    private final VulkanGameRunner runner;

    private Runnable simulatorTerminateCallback = () -> LOG.warn("Simulation terminated before initialization was done!");

    public SimulationRunner(
            final GameMode initialGameMode,
            final String threadName,
            final InputProvider inputProvider,
            final GameRunnerTimeManager timeManager,
            final PresentableStateQueue stateQueue,
            final Runnable onTerminate
    ) {
        final var threadFactory = createThreadFactory(threadName);
        this.executor = Executors.newSingleThreadScheduledExecutor(threadFactory);

        this.timeManager = timeManager;
        this.stateQueue = stateQueue;
        this.onTerminate = onTerminate;

        this.runner = new VulkanGameRunner(this.timeManager, inputProvider, initialGameMode);
    }

    public void start() {
        final var runnerFuture = this.executor.scheduleAtFixedRate(this::tick,
                                                                   0L,
                                                                   this.timeManager.getTimeStep(),
                                                                   TimeUnit.MILLISECONDS);

        this.simulatorTerminateCallback = () -> {
            runnerFuture.cancel(false);
            this.onTerminate.run();
        };
    }

    private void tick() {
        this.runner.simulateTick(this.simulatorTerminateCallback);
        this.timeManager.nextTick();

        final var state = this.stateQueue.swapWriting();
        this.runner.recordPresentableState(state);
    }

    public void terminate() {
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(5L, TimeUnit.SECONDS)) {
                LOG.error("Simulation thread was not terminated within time limit! Forcing shutdown.");
            }
        } catch (final InterruptedException ignored) {
            LOG.warn("Main thread was interrupted while waiting for simulation thread to terminate!");
        }
    }

    private static ThreadFactory createThreadFactory(final String threadName) {
        return r -> new Thread(r, threadName);
    }
}
