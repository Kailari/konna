package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.LWJGLTexture;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.LWJGLTextRenderer;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UILabel;
import fi.jakojaannos.roguelite.engine.view.ui.UIPanel;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

@Slf4j
public class MainMenuRenderingSystem implements ECSSystem, AutoCloseable {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
    }

    private final LWJGLTextRenderer textRenderer;
    private final LWJGLCamera camera;

    private final int screenCameraUbo;
    private final ByteBuffer cameraMatricesData;

    private List<UIElement> uiRoots;

    public MainMenuRenderingSystem(
            LWJGLTextRenderer textRenderer,
            LWJGLCamera camera,
            SpriteRegistry<LWJGLTexture> spriteRegistry,
            SpriteBatch<LWJGLTexture> spriteBatch
    ) {
        this.textRenderer = textRenderer;
        this.camera = camera;

        this.screenCameraUbo = glGenBuffers();
        this.cameraMatricesData = MemoryUtil.memAlloc(2 * 16 * 4);

        val sprite = spriteRegistry.getByAssetName("sprites/ui/ui");
        val halfScreenWidth = this.camera.getViewportWidthInPixels() / 2.0;
        val y = 150;
        val width = 600;
        val height = 100;
        val xOffset = -width / 2.0;
        val x = halfScreenWidth + xOffset;
        val borderSize = 25;
        this.uiRoots = new UIBuilder()
                .withElement(UIPanel.<LWJGLTexture>builder("play_button")
                                     .anchor(0.5, 0.25)
                                     .origin(0.5, 0.0)
                                     .position(0, 5)
                                     .size(width, height)
                                     .borderSize(borderSize)
                                     .sprite(sprite, spriteBatch)
                                     .child(UILabel.builder("play_button_label", this.textRenderer)
                                                   .anchor(0.5, 0.5)
                                                   .origin(0.5, 0.5)
                                                   .position(0, 20)
                                                   .text("Play")
                                                   .fontSize(24)
                                                   .build())
                                     //.onClick((element, event) -> LOG.info("Play clicked!"))
                                     .build())
                .withElement(UILabel.builder("title_label", this.textRenderer)
                                    .anchor(0.5, 0.25)
                                    .origin(0.5, 1.0)
                                    .position(0, 0)
                                    .text("Konna")
                                    .fontSize(48)
                                    .build())
                .build();
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val halfScreenWidth = this.camera.getViewportWidthInPixels() / 2.0;
        //this.textRenderer.drawCenteredOnScreen(halfScreenWidth, 50, 48, "Konna");


        this.camera.useScreenCoordinates();
        this.uiRoots.forEach(uiElement -> uiElement.render(0,
                                                           0,
                                                           this.camera.getViewportWidthInPixels(),
                                                           this.camera.getViewportHeightInPixels()));
    }

    @Override
    public void close() {
        MemoryUtil.memFree(this.cameraMatricesData);
        glDeleteBuffers(this.screenCameraUbo);
    }
}
