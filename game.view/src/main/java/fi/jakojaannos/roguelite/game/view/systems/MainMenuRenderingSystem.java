package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.data.resources.GameStateManager;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.LWJGLTextRenderer;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.Mouse;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

@Slf4j
public class MainMenuRenderingSystem implements ECSSystem, AutoCloseable {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
    }

    private final LWJGLCamera camera;

    private final int screenCameraUbo;
    private final ByteBuffer cameraMatricesData;

    private final UserInterface userInterface;

    public MainMenuRenderingSystem(
            final LWJGLTextRenderer textRenderer,
            final LWJGLCamera camera,
            final Viewport viewport,
            final SpriteRegistry spriteRegistry,
            final SpriteBatch spriteBatch
    ) {
        this.camera = camera;

        this.screenCameraUbo = glGenBuffers();
        this.cameraMatricesData = MemoryUtil.memAlloc(2 * 16 * 4);

        Sprite sprite = spriteRegistry.getByAssetName("sprites/ui/ui");

        val width = 600;
        val height = 100;
        val borderSize = 25;
        this.userInterface = UserInterface
                .builder(viewport, spriteBatch, textRenderer)
                .element("play_button",
                         UIElementType.PANEL,
                         builder -> builder.anchorX(ProportionValue.percentOf().parentWidth(0.5))
                                           .left(ProportionValue.percentOf().ownWidth(-0.5))
                                           .top(ProportionValue.percentOf().parentHeight(0.3))
                                           .width(ProportionValue.absolute(width))
                                           .height(ProportionValue.absolute(height))
                                           .borderSize(borderSize)
                                           .sprite(sprite)
                                           .child("play_button_label",
                                                  UIElementType.LABEL,
                                                  labelBuilder -> labelBuilder
                                                          .anchorX(ProportionValue.percentOf().parentWidth(0.5))
                                                          .anchorY(ProportionValue.percentOf().parentHeight(0.5))
                                                          .left(ProportionValue.percentOf().ownWidth(-0.5))
                                                          .top(ProportionValue.absolute(0))
                                                          .text("Play")
                                                          .fontSize(24)))
                .element("title_label",
                         UIElementType.LABEL,
                         builder -> builder.anchorX(ProportionValue.percentOf().parentWidth(0.5))
                                           .anchorY(ProportionValue.percentOf().parentHeight(0.25))
                                           .left(ProportionValue.percentOf().ownWidth(-0.5))
                                           .top(ProportionValue.percentOf().ownHeight(-1.0))
                                           .text("Konna")
                                           .fontSize(48))
                .build();
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        this.camera.useScreenCoordinates();
        this.userInterface.render();

        val mouse = world.getOrCreateResource(Mouse.class);
        val viewport = this.camera.getViewport();
        val mousePos = mouse.pos.mul(viewport.getWidthInPixels(),
                                     viewport.getHeightInPixels(),
                                     new Vector2d());
        val events = this.userInterface.pollEvents(mousePos,
                                                   world.getOrCreateResource(Inputs.class).inputAttack);
        while (!events.isEmpty()) {
            val event = events.remove();
            if (event.getElement().equalsIgnoreCase("play_button") && event.getType() == UIEvent.Type.CLICK) {
                world.getOrCreateResource(GameStateManager.class)
                     .queueStateChange(new GameplayGameState(System.nanoTime(),
                                                             World.createNew(EntityManager.createNew(256, 32)),
                                                             world.getOrCreateResource(Time.class).getTimeManager()));
            }
        }
    }

    @Override
    public void close() {
        MemoryUtil.memFree(this.cameraMatricesData);
        glDeleteBuffers(this.screenCameraUbo);
    }
}
