package fi.jakojaannos.roguelite.game.view;

import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLWindow;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.LWJGLTexture;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.TextRenderer;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.view.GameRenderer;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.view.content.TextureRegistry;
import fi.jakojaannos.roguelite.game.DebugConfig;
import fi.jakojaannos.roguelite.game.data.components.Camera;
import fi.jakojaannos.roguelite.game.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.game.view.systems.*;
import fi.jakojaannos.roguelite.game.view.systems.debug.EntityCollisionBoundsRenderingSystem;
import fi.jakojaannos.roguelite.game.view.systems.debug.EntityTransformRenderingSystem;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.nio.file.Path;

@Slf4j
public class RogueliteGameRenderer implements GameRenderer<GameState> {
    private final SystemDispatcher rendererDispatcher;
    private final RogueliteCamera camera;
    private final TextureRegistry<LWJGLTexture> textureRegistry;
    private final SpriteRegistry<LWJGLTexture> spriteRegistry;
    private final TextRenderer textRenderer;

    public RogueliteGameRenderer(final Path assetRoot, final LWJGLWindow window) {
        LOG.debug("Constructing GameRenderer...");
        LOG.debug("asset root: {}", assetRoot);

        this.camera = new RogueliteCamera(window.getWidth(), window.getHeight());
        this.textureRegistry = new TextureRegistry<>(assetRoot, LWJGLTexture::new);
        this.spriteRegistry = new SpriteRegistry<>(assetRoot, this.textureRegistry);
        this.textRenderer = new TextRenderer(assetRoot, this.camera);

        val builder = SystemDispatcher.builder()
                                      .withSystem(new LevelRenderingSystem(assetRoot, this.camera, this.spriteRegistry))
                                      .withSystem(new SpriteRenderingSystem(assetRoot, this.camera, this.spriteRegistry))
                                      .withSystem(new RenderHUDSystem(this.textRenderer))
                                      .withSystem(new RenderGameOverSystem(this.textRenderer, this.camera))
                                      .withSystem(new HealthBarRenderingSystem(assetRoot, this.camera));

        if (DebugConfig.debugModeEnabled) {
            builder.withSystem(new EntityCollisionBoundsRenderingSystem(assetRoot, this.camera));
            builder.withSystem(new EntityTransformRenderingSystem(assetRoot, this.camera));
        }
        this.rendererDispatcher = builder.build();

        window.addResizeCallback(this.camera::resizeViewport);

        LOG.info("GameRenderer initialization finished.");
    }

    @Override
    public void render(GameState state, double partialTickAlpha) {
        // Make sure that the camera configuration matches the current state
        this.camera.updateConfigurationFromState(state);

        // Snap camera to active camera
        val cameraEntity = state.getWorld().getOrCreateResource(CameraProperties.class).cameraEntity;
        state.getWorld().getEntityManager().getComponentOf(cameraEntity, Camera.class)
             .ifPresent(cam -> this.camera.setPosition(cam.pos.x - this.camera.getViewportWidthInUnits() / 2.0,
                                                       cam.pos.y - this.camera.getViewportHeightInUnits() / 2.0));
        this.rendererDispatcher.dispatch(state.getWorld());
    }

    @Override
    public void close() throws Exception {
        this.rendererDispatcher.close();
        this.textureRegistry.close();
        this.spriteRegistry.close();
        this.textRenderer.close();
    }
}
