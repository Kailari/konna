package fi.jakojaannos.konna.engine.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import fi.jakojaannos.konna.engine.assets.AssetManager;
import fi.jakojaannos.konna.engine.view.RenderDispatcher;
import fi.jakojaannos.konna.view.adapters.PlayerCharacterRenderAdapter;
import fi.jakojaannos.konna.engine.view.renderer.RendererRecorder;
import fi.jakojaannos.konna.view.adapters.EntityTransformRenderAdapter;
import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameRunnerTimeManager;
import fi.jakojaannos.roguelite.engine.input.InputProvider;

public class SimulationThread implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(SimulationThread.class);

    private final RendererRecorder renderer;

    private final ScheduledExecutorService executor;
    private final GameRunnerTimeManager timeManager;
    private final Runnable onTerminate;

    private final GameTicker ticker;
    private final RenderDispatcher renderDispatcher;
    private final PresentableStateQueue presentableStateQueue;

    private Runnable simulatorTerminateCallback = () -> LOG.warn("Simulation terminated before initialization was done!");

    public SimulationThread(
            final AssetManager assetManager,
            final GameMode initialGameMode,
            final String threadName,
            final InputProvider inputProvider,
            final GameRunnerTimeManager timeManager,
            final RendererRecorder renderer,
            final Runnable onTerminate
    ) {
        this.renderer = renderer;

        final var threadFactory = createThreadFactory(threadName);
        this.executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
        this.presentableStateQueue = new PresentableStateQueue();

        this.timeManager = timeManager;
        this.onTerminate = onTerminate;

        this.ticker = new GameTicker(this.timeManager, inputProvider, initialGameMode);
        this.renderDispatcher = RenderDispatcher.builder()
                                                .withAdapter(new EntityTransformRenderAdapter())
                                                .withAdapter(new PlayerCharacterRenderAdapter(assetManager))
                                                .build();

        startSimulation();
    }

    private void startSimulation() {
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
        try {
            this.ticker.simulateTick(this.simulatorTerminateCallback);
        } catch (final Throwable t) {
            LOG.error("Simulation tick encountered an error:", t);
        }

        try {
            final var state = this.presentableStateQueue.swapWriting();
            state.clear(this.timeManager);

            this.renderer.setWriteState(state);
            this.renderDispatcher.dispatch(this.renderer,
                                           this.ticker.getState(),
                                           0L);
        } catch (final Throwable t) {
            LOG.error("Render adapter dispatcher encountered an error:", t);
        }

        this.timeManager.nextTick();
    }

    @Override
    public void close() {
        LOG.info("Simulation thread shutting down");
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(5L, TimeUnit.SECONDS)) {
                LOG.error("Simulation thread was not terminated within time limit! Forcing shutdown.");
            }
        } catch (final InterruptedException ignored) {
            LOG.warn("Main thread was interrupted while waiting for simulation thread to terminate!");
        }
    }

    public PresentableState fetchPresentableState() {
        return this.presentableStateQueue.swapReading();
    }

    private static ThreadFactory createThreadFactory(final String threadName) {
        return r -> new Thread(r, threadName);
    }
}
