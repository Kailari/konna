package fi.jakojaannos.riista.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import fi.jakojaannos.riista.data.resources.CameraProperties;
import fi.jakojaannos.riista.GameRenderAdapter;
import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameRunnerTimeManager;
import fi.jakojaannos.roguelite.engine.input.InputProvider;

public class SimulationThread implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(SimulationThread.class);

    private final ScheduledExecutorService executor;
    private final GameRunnerTimeManager timeManager;
    private final Runnable onTerminate;

    private final GameTicker ticker;

    private final Consumer<CameraProperties> cameraPropertiesUpdater;

    @Nullable
    private final GameRenderAdapter<?> renderAdapter;

    private Runnable simulatorTerminateCallback = () -> LOG.warn("Simulation terminated before initialization was done!");

    public SimulationThread(
            final GameMode initialGameMode,
            final String threadName,
            final InputProvider inputProvider,
            final GameRunnerTimeManager timeManager,
            final Runnable onTerminate,
            final Consumer<CameraProperties> cameraPropertiesUpdater,
            @Nullable final GameRenderAdapter<?> renderAdapter
    ) {
        this.cameraPropertiesUpdater = cameraPropertiesUpdater;
        this.renderAdapter = renderAdapter;

        final var threadFactory = createThreadFactory(threadName);
        this.executor = Executors.newSingleThreadScheduledExecutor(threadFactory);

        this.timeManager = timeManager;
        this.onTerminate = onTerminate;

        this.ticker = new GameTicker(this.timeManager, inputProvider, initialGameMode);
        if (this.renderAdapter != null) {
            this.renderAdapter.onGameModeChange(initialGameMode, this.ticker.getState());
        }
    }

    public void startSimulation() {
        final var runnerFuture = this.executor.scheduleAtFixedRate(this::tick,
                                                                   0L,
                                                                   this.timeManager.getTimeStep(),
                                                                   TimeUnit.MILLISECONDS);

        this.simulatorTerminateCallback = () -> {
            runnerFuture.cancel(false);
            this.onTerminate.run();
        };
    }

    public void tick() {
        final var oldMode = this.ticker.getMode();
        try {
            final var cameraProperties = this.ticker.getState().world()
                                                    .fetchResource(CameraProperties.class);
            this.cameraPropertiesUpdater.accept(cameraProperties);

            this.ticker.simulateTick(this.simulatorTerminateCallback);
        } catch (final Throwable t) {
            LOG.error("Simulation tick encountered an error:", t);
        }

        if (this.renderAdapter != null) {
            final var currentMode = this.ticker.getMode();
            final var currentState = this.ticker.getState();

            final var modeHasChanged = currentMode != oldMode;
            if (modeHasChanged) {
                this.renderAdapter.onGameModeChange(currentMode, currentState);
            }

            try {
                final var systemEvents = this.ticker.getSystemEvents();
                this.renderAdapter.writePresentableState(currentState, systemEvents);
            } catch (final Throwable t) {
                LOG.error("Render adapter dispatcher encountered an error:", t);
            }
        }

        this.timeManager.nextTick();
    }

    @Override
    public void close() {
        LOG.info("Simulation thread shutting down");

        if (this.renderAdapter != null) {
            this.renderAdapter.close();
        }

        LOG.debug("Killing executor service");
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
