package fi.jakojaannos.riista.vulkan.renderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import fi.jakojaannos.riista.GameRenderAdapter;
import fi.jakojaannos.riista.data.resources.CameraProperties;
import fi.jakojaannos.riista.view.GameModeRenderers;
import fi.jakojaannos.riista.GameMode;
import fi.jakojaannos.riista.GameState;
import fi.jakojaannos.riista.ecs.SystemDispatcher;

public abstract class GameRenderAdapterBase<TPresentState> implements GameRenderAdapter<TPresentState> {
    private static final Logger LOG = LoggerFactory.getLogger(GameRenderAdapter.class);

    private final GameModeRenderers gameModeRenderers;
    private final Consumer<CameraProperties> cameraPropertiesUpdater;

    @Nullable
    private SystemDispatcher renderDispatcher;

    protected GameRenderAdapterBase(
            final GameModeRenderers gameModeRenderers,
            final Consumer<CameraProperties> cameraPropertiesUpdater
    ) {
        this.gameModeRenderers = gameModeRenderers;
        this.cameraPropertiesUpdater = cameraPropertiesUpdater;
    }

    public boolean hasActiveRenderDispatcher() {
        return this.renderDispatcher != null;
    }

    @Override
    public void onGameModeChange(final GameMode gameMode, final GameState gameState) {
        if (this.renderDispatcher != null) {
            this.renderDispatcher.close();
        }

        final var maybeRenderDispatcher = this.gameModeRenderers.get(gameMode.id());
        maybeRenderDispatcher.ifPresent(dispatcher -> {
            gameState.systems().resetToDefaultState(dispatcher.getSystems());
            gameState.systems().resetGroupsToDefaultState(dispatcher.getGroups());
        });

        this.renderDispatcher = maybeRenderDispatcher.orElse(null);
    }

    @Override
    public void preTick(final GameState gameState) {
        final var cameraProperties = gameState.world()
                                              .fetchResource(CameraProperties.class);
        this.cameraPropertiesUpdater.accept(cameraProperties);
    }

    @Override
    public void writePresentableState(final GameState gameState, final Collection<Object> events) {
        if (this.renderDispatcher != null) {
            this.renderDispatcher.tick(gameState.world(), gameState.systems(), events);
        }
    }

    @Override
    public void close() {
        if (this.renderDispatcher != null) {
            try {
                LOG.debug("Render dispatcher closing");
                this.renderDispatcher.close();
            } catch (final Throwable t) {
                LOG.error("Disposing render dispatcher failed", t);
            }
        } else {
            LOG.warn("No render dispatcher present.");
        }
    }
}
