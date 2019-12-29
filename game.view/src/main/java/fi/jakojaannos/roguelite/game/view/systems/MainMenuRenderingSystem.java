package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.data.resources.GameStateManager;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.LWJGLFont;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterfaceRenderer;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.Mouse;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

@Slf4j
public class MainMenuRenderingSystem implements ECSSystem, AutoCloseable {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
    }

    private final LWJGLFont font;

    private final LWJGLCamera camera;

    private final int screenCameraUbo;
    private final ByteBuffer cameraMatricesData;

    private final UserInterfaceRenderer userInterfaceRenderer;

    public MainMenuRenderingSystem(
            final Path assetRoot,
            final LWJGLCamera camera,
            final Viewport viewport,
            final SpriteRegistry spriteRegistry,
            final SpriteBatch spriteBatch,
            final TextRenderer textRenderer
    ) {
        this.camera = camera;

        this.screenCameraUbo = glGenBuffers();
        this.cameraMatricesData = MemoryUtil.memAlloc(2 * 16 * 4);

        this.font = new LWJGLFont(assetRoot, 1.0f, 1.0f);
        this.userInterfaceRenderer = new UserInterfaceRenderer(spriteBatch, spriteRegistry, textRenderer, this.font);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val userInterface = world.getOrCreateResource(UserInterface.class);
        val mouse = world.getOrCreateResource(Mouse.class);
        val viewport = this.camera.getViewport();
        val mousePos = mouse.pos.mul(viewport.getWidthInPixels(),
                                     viewport.getHeightInPixels(),
                                     new Vector2d());
        // FIXME: Updating should not happen in the renderer
        val events = userInterface.update(mousePos,
                                          world.getOrCreateResource(Inputs.class).inputAttack);
        while (!events.isEmpty()) {
            val event = events.remove();
            if (event.getElement().equalsIgnoreCase("play_button") && event.getType() == UIEvent.Type.CLICK) {
                world.getOrCreateResource(GameStateManager.class)
                     .queueStateChange(new GameplayGameState(System.nanoTime(),
                                                             World.createNew(EntityManager.createNew(256, 32)),
                                                             world.getOrCreateResource(Time.class).getTimeManager(),
                                                             userInterface.getViewportSizeProvider(),
                                                             userInterface.getTextSizeProvider()));
            }
        }

        this.camera.useScreenCoordinates();
        this.userInterfaceRenderer.render(userInterface);
    }

    @Override
    public void close() {
        MemoryUtil.memFree(this.cameraMatricesData);
        glDeleteBuffers(this.screenCameraUbo);
        this.font.close();
    }
}
