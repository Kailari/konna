package fi.jakojaannos.roguelite.game.view;

import fi.jakojaannos.roguelite.engine.content.AssetManager;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.GameRenderer;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.Window;
import fi.jakojaannos.roguelite.engine.view.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.text.Font;
import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;
import fi.jakojaannos.roguelite.game.state.MainMenuGameState;
import fi.jakojaannos.roguelite.game.view.state.GameStateRenderer;
import fi.jakojaannos.roguelite.game.view.state.GameplayGameStateRenderer;
import fi.jakojaannos.roguelite.game.view.state.MainMenuGameStateRenderer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class RogueliteGameRenderer implements GameRenderer {
    private final Camera camera;
    private final AssetManager assetManager;

    private final TextRenderer textRenderer;

    private final Map<Class<? extends GameState>, GameStateRenderer> stateRenderers;

    public RogueliteGameRenderer(
            final Path assetRoot,
            final Window window,
            final RenderingBackend backend,
            final AssetManager assetManager
    ) {
        LOG.trace("Constructing GameRenderer...");

        this.assetManager = assetManager;
        val spriteRegistry = assetManager.getAssetRegistry(Sprite.class);
        val fontRegistry = assetManager.getAssetRegistry(Font.class);

        val viewport = backend.getViewport(window);
        this.camera = backend.getCamera(viewport);
        this.textRenderer = backend.getTextRenderer(assetRoot, this.camera);

        this.stateRenderers = Map.ofEntries(
                Map.entry(GameplayGameState.class, new GameplayGameStateRenderer(assetRoot,
                                                                                 this.camera,
                                                                                 viewport,
                                                                                 spriteRegistry,
                                                                                 fontRegistry,
                                                                                 this.textRenderer,
                                                                                 backend)),
                Map.entry(MainMenuGameState.class, new MainMenuGameStateRenderer(assetRoot,
                                                                                 this.camera,
                                                                                 this.textRenderer,
                                                                                 spriteRegistry,
                                                                                 fontRegistry,
                                                                                 backend))
        );

        window.addResizeCallback(viewport::resize);
        window.addResizeCallback(this.camera::resize);
        viewport.resize(window.getWidth(), window.getHeight());
        this.camera.resize(window.getWidth(), window.getHeight());

        LOG.trace("GameRenderer initialization finished.");
    }

    @Override
    public void render(final GameState state, final double partialTickAlpha, final Events events) {
        // Snap camera to active camera
        val world = state.getWorld();
        val entityManager = world.getEntityManager();
        Optional.ofNullable(world.getOrCreateResource(CameraProperties.class).cameraEntity)
                .flatMap(cameraEntity -> entityManager.getComponentOf(cameraEntity, Transform.class))
                .ifPresent(cameraTransform -> this.camera.setPosition(cameraTransform.position.x - this.camera.getVisibleAreaWidth() / 2.0,
                                                                      cameraTransform.position.y - this.camera.getVisibleAreaHeight() / 2.0));

        // Make sure that the camera configuration matches the current state
        this.camera.updateConfigurationFromState(state);

        Optional.ofNullable(this.stateRenderers.get(state.getClass()))
                .ifPresent(renderer -> renderer.render(state.getWorld()));
    }

    @Override
    public void close() throws Exception {
        this.stateRenderers.values().forEach(renderer -> {
            try {
                renderer.close();
            } catch (Exception ignored) {
            }
        });
        this.assetManager.close();
        this.textRenderer.close();
        //this.camera.close();
    }
}
