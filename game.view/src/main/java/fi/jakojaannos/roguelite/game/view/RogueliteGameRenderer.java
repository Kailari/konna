package fi.jakojaannos.roguelite.game.view;

import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.LWJGLTexture;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.LWJGLFont;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.view.*;
import fi.jakojaannos.roguelite.engine.view.content.FontRegistry;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.view.content.TextureRegistry;
import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;
import fi.jakojaannos.roguelite.game.state.MainMenuGameState;
import fi.jakojaannos.roguelite.game.view.state.GameStateRenderer;
import fi.jakojaannos.roguelite.game.view.state.GameplayGameStateRenderer;
import fi.jakojaannos.roguelite.game.view.state.MainMenuGameStateRenderer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class RogueliteGameRenderer implements GameRenderer<GameState> {
    private final Camera camera;
    @Getter private final Viewport viewport;
    private final TextureRegistry textureRegistry;
    private final SpriteRegistry spriteRegistry;
    private final FontRegistry fontRegistry;
    @Getter private final TextRenderer textRenderer;

    private final Map<Class<? extends GameState>, GameStateRenderer> stateRenderers;

    public RogueliteGameRenderer(
            final Path assetRoot,
            final Window window,
            final RenderingBackend backend
    ) {
        LOG.debug("Constructing GameRenderer...");
        LOG.debug("asset root: {}", assetRoot);

        // TODO: Extract to "AssetManager"
        this.textureRegistry = new TextureRegistry(assetRoot, LWJGLTexture::new);
        this.spriteRegistry = new SpriteRegistry(assetRoot, this.textureRegistry);
        this.fontRegistry = new FontRegistry(assetRoot, LWJGLFont::new);

        this.viewport = backend.createViewport(window);
        this.camera = backend.getCamera(this.viewport);
        this.textRenderer = backend.getTextRenderer(assetRoot, this.camera);

        this.stateRenderers = Map.ofEntries(
                Map.entry(GameplayGameState.class, new GameplayGameStateRenderer(assetRoot,
                                                                                 this.camera,
                                                                                 this.viewport,
                                                                                 this.spriteRegistry,
                                                                                 this.fontRegistry,
                                                                                 this.textRenderer,
                                                                                 backend)),
                Map.entry(MainMenuGameState.class, new MainMenuGameStateRenderer(assetRoot,
                                                                                 this.camera,
                                                                                 this.textRenderer,
                                                                                 this.spriteRegistry,
                                                                                 this.fontRegistry,
                                                                                 backend))
        );

        window.addResizeCallback(this.viewport::resize);
        window.addResizeCallback(this.camera::resize);
        this.viewport.resize(window.getWidth(), window.getHeight());
        this.camera.resize(window.getWidth(), window.getHeight());

        LOG.info("GameRenderer initialization finished.");
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
        // TODO: Move to some Camera.refresh() -method or sth.
        ((LWJGLCamera) this.camera).updateConfigurationFromState(state.getWorld());
        ((LWJGLCamera) this.camera).refreshMatricesIfDirty();

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
        this.fontRegistry.close();
        //this.camera.close();
    }
}
