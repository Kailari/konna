package fi.jakojaannos.roguelite.game.view;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import fi.jakojaannos.roguelite.engine.content.AssetManager;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.GameRenderer;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.Window;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;
import fi.jakojaannos.roguelite.game.state.MainMenuGameState;
import fi.jakojaannos.roguelite.game.view.state.GameStateRenderer;
import fi.jakojaannos.roguelite.game.view.state.GameplayGameStateRenderer;
import fi.jakojaannos.roguelite.game.view.state.MainMenuGameStateRenderer;

@Slf4j
public class RogueliteGameRenderer implements GameRenderer {
    @Getter private final Camera camera;

    private final Map<Class<? extends GameState>, GameStateRenderer> stateRenderers;

    public RogueliteGameRenderer(
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

        this.stateRenderers = Map.ofEntries(
                Map.entry(GameplayGameState.class, new GameplayGameStateRenderer(assetRoot,
                                                                                 this.camera,
                                                                                 assetManager,
                                                                                 backend)),
                Map.entry(MainMenuGameState.class, new MainMenuGameStateRenderer(assetRoot,
                                                                                 this.camera,
                                                                                 assetManager,
                                                                                 backend))
        );

        LOG.trace("GameRenderer initialization finished.");
    }

    @Override
    public void render(final GameState state, final double partialTickAlpha, final Events events) {
        // Make sure that the camera configuration matches the current state
        this.camera.updateConfigurationFromState(state);

        // Snap camera to active camera
        final var world = state.getWorld();
        final var entityManager = world.getEntityManager();
        Optional.ofNullable(world.getOrCreateResource(CameraProperties.class).cameraEntity)
                .flatMap(cameraEntity -> entityManager.getComponentOf(cameraEntity, Transform.class))
                .ifPresent(cameraTransform -> this.camera.setPosition(cameraTransform.position.x,
                                                                      cameraTransform.position.y));

        Optional.ofNullable(this.stateRenderers.get(state.getClass()))
                .ifPresent(renderer -> renderer.render(state.getWorld()));
    }

    public UserInterface getUserInterfaceForState(final GameState state) {
        return Optional.ofNullable(this.stateRenderers.get(state.getClass()))
                       .map(GameStateRenderer::getUserInterface)
                       .orElseThrow();
    }

    @Override
    public void close() throws Exception {
        this.stateRenderers.values().forEach(renderer -> {
            try {
                renderer.close();
            } catch (Exception ignored) {
            }
        });
        this.camera.close();
    }
}
