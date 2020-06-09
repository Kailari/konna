package fi.jakojaannos.riista.vulkan.application;

import org.lwjgl.vulkan.VkExtent2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.view.GameModeRenderers;
import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.riista.vulkan.renderer.RendererRecorder;
import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameRunnerTimeManager;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.input.InputProvider;

public class SimulationThread implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(SimulationThread.class);

    private final GameModeRenderers gameModeRenderers;
    private final RendererRecorder renderRecorder;

    private final ScheduledExecutorService executor;
    private final GameRunnerTimeManager timeManager;
    private final Runnable onTerminate;

    private final GameTicker ticker;
    private final PresentableStateQueue presentableStateQueue;

    private final Supplier<VkExtent2D> swapchainExtentSupplier;
    private final Consumer<CameraProperties> cameraPropertiesUpdater;

    private SystemDispatcher renderDispatcher;

    private Runnable simulatorTerminateCallback = () -> LOG.warn("Simulation terminated before initialization was done!");

    public SimulationThread(
            final AssetManager assetManager,
            final GameMode initialGameMode,
            final String threadName,
            final InputProvider inputProvider,
            final GameRunnerTimeManager timeManager,
            final RendererRecorder renderRecorder,
            final Runnable onTerminate,
            final Supplier<VkExtent2D> swapchainExtentSupplier,
            final Consumer<CameraProperties> cameraPropertiesUpdater,
            final GameModeRenderers gameModeRenderers
    ) {
        this.renderRecorder = renderRecorder;
        this.swapchainExtentSupplier = swapchainExtentSupplier;
        this.cameraPropertiesUpdater = cameraPropertiesUpdater;
        this.gameModeRenderers = gameModeRenderers;

        final var threadFactory = createThreadFactory(threadName);
        this.executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
        this.presentableStateQueue = new PresentableStateQueue();

        this.timeManager = timeManager;
        this.onTerminate = onTerminate;

        this.ticker = new GameTicker(this.timeManager, inputProvider, initialGameMode);

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
        final var oldMode = this.ticker.getMode();
        try {
            final var cameraProperties = this.ticker.getState().world()
                                                    .fetchResource(CameraProperties.class);
            this.cameraPropertiesUpdater.accept(cameraProperties);

            this.ticker.simulateTick(this.simulatorTerminateCallback);
        } catch (final Throwable t) {
            LOG.error("Simulation tick encountered an error:", t);
        }

        try {
            final var presentableState = this.presentableStateQueue.swapWriting();
            final var currentState = this.ticker.getState();
            final var currentMode = this.ticker.getMode();

            final var modeHasChanged = currentMode != oldMode;
            if (modeHasChanged || this.renderDispatcher == null) {
                if (this.renderDispatcher != null) {
                    this.renderDispatcher.close();
                }

                final var maybeRenderDispatcher = this.gameModeRenderers.get(currentMode.id());
                maybeRenderDispatcher.ifPresent(dispatcher -> {
                    currentState.systems().resetToDefaultState(dispatcher.getSystems());
                    currentState.systems().resetGroupsToDefaultState(dispatcher.getGroups());
                });

                this.renderDispatcher = maybeRenderDispatcher.orElse(null);
            }

            if (this.renderDispatcher != null) {
                final var cameraProperties = this.ticker.getState().world()
                                                        .fetchResource(CameraProperties.class);
                final var mouse = this.ticker.getState().world()
                                             .fetchResource(Mouse.class);

                // FIXME: Clear the buffers BEFORE ticking the simulation, update only camera etc. post-tick
                //  - still avoids camera lag
                //  - allows debug rendering from regular systems (!!)
                presentableState.clear(currentMode.id(),
                                       this.timeManager,
                                       mouse.position,
                                       mouse.clicked,
                                       this.swapchainExtentSupplier.get(),
                                       cameraProperties.getPosition(),
                                       cameraProperties.getViewMatrix());

                currentState.world().replaceResource(Renderer.class, this.renderRecorder);
                this.renderRecorder.setWriteState(presentableState);

                final var systemEvents = this.ticker.getSystemEvents();
                this.renderDispatcher.tick(currentState.world(), currentState.systems(), systemEvents);
            }
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
