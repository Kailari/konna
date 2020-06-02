package fi.jakojaannos.konna.engine.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import fi.jakojaannos.konna.engine.assets.AssetManager;
import fi.jakojaannos.konna.engine.view.GameModeRenderers;
import fi.jakojaannos.konna.engine.view.RenderDispatcher;
import fi.jakojaannos.konna.engine.view.renderer.RendererRecorder;
import fi.jakojaannos.konna.view.adapters.*;
import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameRunnerTimeManager;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.input.InputProvider;
import fi.jakojaannos.roguelite.game.gamemode.GameplayGameMode;

public class SimulationThread implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(SimulationThread.class);

    private final GameModeRenderers gameModeRenderers = new GameModeRenderers();
    private final RendererRecorder renderRecorder;

    private final ScheduledExecutorService executor;
    private final GameRunnerTimeManager timeManager;
    private final Runnable onTerminate;

    private final GameTicker ticker;
    private final PresentableStateQueue presentableStateQueue;

    private final Consumer<CameraProperties> cameraPropertiesUpdater;

    private RenderDispatcher renderDispatcher;
    private int oldModeId = -1;

    private Runnable simulatorTerminateCallback = () -> LOG.warn("Simulation terminated before initialization was done!");

    public SimulationThread(
            final AssetManager assetManager,
            final GameMode initialGameMode,
            final String threadName,
            final InputProvider inputProvider,
            final GameRunnerTimeManager timeManager,
            final RendererRecorder renderRecorder,
            final Runnable onTerminate,
            final Consumer<CameraProperties> cameraPropertiesUpdater
    ) {
        this.renderRecorder = renderRecorder;
        this.cameraPropertiesUpdater = cameraPropertiesUpdater;

        final var threadFactory = createThreadFactory(threadName);
        this.executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
        this.presentableStateQueue = new PresentableStateQueue();

        this.timeManager = timeManager;
        this.onTerminate = onTerminate;

        this.ticker = new GameTicker(this.timeManager, inputProvider, initialGameMode);

        // FIXME: Standardize constructor arguments and load adapters from .json?
        //  - constructor args should be something like (assetManager, config)
        //  - config can be generic object of type TConfig which is _optionally_ populated if system
        //    overrides method getConfigClass to return non-null values. The parameter value can
        //    then be nullable
        //  - the config object is then consumed in the constructor to set values for the actual
        //    configuration fields (which should be final) on the adapter
        this.gameModeRenderers.register(GameplayGameMode.GAME_MODE_ID, () -> RenderDispatcher
                .builder()
                .withAdapter(new EntityTransformRenderAdapter())
                .withAdapter(new PlayerCharacterRenderAdapter(assetManager))
                //.withAdapter(new CharacterHealthbarRenderAdapter(timeManager.convertToTicks(5.0)))
                .withAdapter(new SessionStatsHudRenderAdapter(assetManager))
                .withAdapter(new GameOverSplashHudRenderAdapter(assetManager))
                .withAdapter(new HordeMessageHudRenderAdapter(assetManager,
                                                              timeManager.convertToTicks(4.0)))
                .build());

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
        final var cameraProperties = this.ticker.getState().world()
                                                .fetchResource(CameraProperties.class);
        try {
            this.cameraPropertiesUpdater.accept(cameraProperties);

            this.ticker.simulateTick(this.simulatorTerminateCallback);
        } catch (final Throwable t) {
            LOG.error("Simulation tick encountered an error:", t);
        }

        try {
            final var state = this.presentableStateQueue.swapWriting();
            final var modeId = this.ticker.getMode().id();

            final var modeHasChanged = modeId != this.oldModeId;
            if (modeHasChanged) {
                this.renderDispatcher = this.gameModeRenderers.get(modeId)
                                                              .orElse(null);
                this.oldModeId = modeId;
            }

            if (this.renderDispatcher != null) {
                state.clear(modeId,
                            this.timeManager,
                            cameraProperties.getPosition(),
                            cameraProperties.getViewMatrix());

                this.renderRecorder.setWriteState(state);
                this.renderDispatcher.dispatch(this.renderRecorder,
                                               this.ticker.getState(),
                                               0L);
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
