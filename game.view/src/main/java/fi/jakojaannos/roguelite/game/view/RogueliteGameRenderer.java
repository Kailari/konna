package fi.jakojaannos.roguelite.game.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.content.AssetManager;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.view.*;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.game.gamemode.GameplayGameMode;
import fi.jakojaannos.roguelite.game.gamemode.MainMenuGameMode;
import fi.jakojaannos.roguelite.game.view.gamemode.GameplayGameModeRenderer;
import fi.jakojaannos.roguelite.game.view.gamemode.MainMenuGameModeRenderer;

public class RogueliteGameRenderer implements GameRenderer {
    private static final Logger LOG = LoggerFactory.getLogger(RogueliteGameRenderer.class);

    private final Camera camera;
    private final GameModeRendererFactory stateRenderers = new GameModeRendererFactory();

    @Nullable
    private GameModeRenderer stateRenderer;

    public Camera getCamera() {
        return this.camera;
    }

    public RogueliteGameRenderer(
            final Events events,
            final Path assetRoot,
            final Window window,
            final RenderingBackend backend,
            final AssetManager assetManager
    ) {
        LOG.trace("Constructing GameRenderer...");

        final var viewport = backend.getViewport(window);
        this.camera = backend.createCamera(viewport);
        window.addResizeCallback(viewport::resize);
        window.addResizeCallback(this.camera::resize);
        viewport.resize(window.getWidth(), window.getHeight());
        this.camera.resize(window.getWidth(), window.getHeight());

        this.stateRenderers.register(GameplayGameMode.GAME_MODE_ID,
                                     (mode) -> GameplayGameModeRenderer.create(events,
                                                                               assetRoot,
                                                                               this.camera,
                                                                               assetManager,
                                                                               backend));
        this.stateRenderers.register(MainMenuGameMode.GAME_MODE_ID,
                                     (mode) -> MainMenuGameModeRenderer.create(events,
                                                                               assetRoot,
                                                                               this.camera,
                                                                               assetManager,
                                                                               backend));

        LOG.trace("GameRenderer initialization finished.");
    }

    @Override
    public void render(final GameState state, final long accumulator) {
        // Make sure that the camera configuration matches the current state
        this.camera.updateConfigurationFromState(state);

        // Snap camera to active camera
        final var world = state.world();
        final var entityManager = world.getEntityManager();
        Optional.ofNullable(world.fetchResource(CameraProperties.class).cameraEntity)
                .flatMap(cameraEntity -> entityManager.getComponentOf(cameraEntity, Transform.class))
                .ifPresent(cameraTransform -> this.camera.setPosition(cameraTransform.position.x,
                                                                      cameraTransform.position.y));

        Optional.ofNullable(this.stateRenderer)
                .ifPresent(renderer -> renderer.renderDispatcher()
                                               .tick(state.world()));
    }

    @Override
    public void changeGameMode(final GameMode gameMode) {
        this.stateRenderer = this.stateRenderers.get(gameMode);
    }

    public UserInterface getUserInterfaceForMode(final GameMode mode) {
        return Optional.ofNullable(this.stateRenderers.get(mode))
                       .map(GameModeRenderer::userInterface)
                       .orElseThrow();
    }

    @Override
    public void close() throws Exception {
        if (this.stateRenderer != null) {
            this.stateRenderer.close();
        }
        this.camera.close();
    }
}
