package fi.jakojaannos.roguelite.game.view;

import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLWindow;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.LWJGLTexture;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.TextRenderer;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.view.GameRenderer;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.view.content.TextureRegistry;
import fi.jakojaannos.roguelite.game.data.components.Camera;
import fi.jakojaannos.roguelite.game.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;
import fi.jakojaannos.roguelite.game.view.state.GameStateRenderer;
import fi.jakojaannos.roguelite.game.view.state.GameplayGameStateRenderer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class RogueliteGameRenderer implements GameRenderer<GameState> {
    private final RogueliteCamera camera;
    private final TextureRegistry<LWJGLTexture> textureRegistry;
    private final SpriteRegistry<LWJGLTexture> spriteRegistry;
    private final TextRenderer textRenderer;

    private final Map<Class<? extends GameState>, GameStateRenderer> stateRenderers;

    public RogueliteGameRenderer(final Path assetRoot, final LWJGLWindow window) {
        LOG.debug("Constructing GameRenderer...");
        LOG.debug("asset root: {}", assetRoot);

        this.camera = new RogueliteCamera(window.getWidth(), window.getHeight());
        this.textureRegistry = new TextureRegistry<>(assetRoot, LWJGLTexture::new);
        this.spriteRegistry = new SpriteRegistry<>(assetRoot, this.textureRegistry);
        this.textRenderer = new TextRenderer(assetRoot, this.camera);

        this.stateRenderers = Map.ofEntries(
                Map.entry(GameplayGameState.class, new GameplayGameStateRenderer(assetRoot,
                                                                                 this.camera,
                                                                                 this.spriteRegistry,
                                                                                 this.textRenderer))
        );

        window.addResizeCallback(this.camera::resizeViewport);

        LOG.info("GameRenderer initialization finished.");
    }

    @Override
    public void render(GameState state, double partialTickAlpha) {
        // Make sure that the camera configuration matches the current state
        this.camera.updateConfigurationFromState(state);

        // Snap camera to active camera
        val world = state.getWorld();
        val entityManager = world.getEntityManager();
        Optional.ofNullable(world.getOrCreateResource(CameraProperties.class).cameraEntity)
                .flatMap(cameraEntity -> entityManager.getComponentOf(cameraEntity, Camera.class))
                .ifPresent(camera -> this.camera.setPosition(camera.pos.x - this.camera.getViewportWidthInUnits() / 2.0,
                                                             camera.pos.y - this.camera.getViewportHeightInUnits() / 2.0));

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
        this.textureRegistry.close();
        this.spriteRegistry.close();
        this.textRenderer.close();
    }
}
