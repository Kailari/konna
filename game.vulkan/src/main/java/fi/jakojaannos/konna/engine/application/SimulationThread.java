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
import fi.jakojaannos.konna.engine.view.Renderer;
import fi.jakojaannos.konna.engine.view.renderer.RendererRecorder;
import fi.jakojaannos.konna.view.adapters.gameplay.*;
import fi.jakojaannos.konna.view.adapters.menu.MainMenuRenderAdapter;
import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameRunnerTimeManager;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.input.InputProvider;
import fi.jakojaannos.roguelite.game.gamemode.GameplayGameMode;
import fi.jakojaannos.roguelite.game.gamemode.MainMenuGameMode;

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
        this.gameModeRenderers.register(GameplayGameMode.GAME_MODE_ID, () -> {
            final var builder = SystemDispatcher.builder();
            builder.group("debug")
                   .withSystem(new EntityTransformRenderAdapter())
                   .withSystem(new EntityColliderRenderAdapter())
                   .buildGroup();
            builder.group("entities")
                   .withSystem(new PlayerCharacterRenderAdapter(assetManager))
                   .buildGroup();
            builder.group("ui")
                   //.withAdapter(new CharacterHealthbarRenderAdapter(timeManager.convertToTicks(5.0)))
                   .withSystem(new SessionStatsHudRenderAdapter(assetManager))
                   .withSystem(new GameOverSplashHudRenderAdapter(assetManager))
                   .withSystem(new HordeMessageHudRenderAdapter(assetManager, timeManager.convertToTicks(4.0)))
                   .buildGroup();

            return builder.build();
        });
        this.gameModeRenderers.register(MainMenuGameMode.GAME_MODE_ID, () -> {
            final var builder = SystemDispatcher.builder();
            builder.group("ui")
                   .withSystem(new MainMenuRenderAdapter(assetManager))
                   .buildGroup();

            return builder.build();
        });

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

                // FIXME: Clear the buffers BEFORE ticking the simulation, update only camera etc. post-tick
                //  - still avoids camera lag
                //  - allows debug rendering from regular systems (!!)
                presentableState.clear(currentMode.id(),
                                       this.timeManager,
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
