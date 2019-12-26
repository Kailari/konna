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
import fi.jakojaannos.roguelite.engine.view.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
            final SpriteRegistry<LWJGLTexture> spriteRegistry,
            final SpriteBatch<LWJGLTexture> spriteBatch
    ) {
        this.camera = camera;

        this.screenCameraUbo = glGenBuffers();
        this.cameraMatricesData = MemoryUtil.memAlloc(2 * 16 * 4);

        Sprite<LWJGLTexture> sprite = spriteRegistry.getByAssetName("sprites/ui/ui");

        val width = 600;
        val height = 100;
        val borderSize = 25;
        this.userInterface = UserInterface
                .builder(camera, spriteBatch, spriteRegistry)
                .element("play_button",
                         UIElementType.PANEL,
                         builder -> builder.anchorX(ProportionValue.percent(0.5))
                                           .left(ProportionValue.absolute(-width / 2))
                                           .top(ProportionValue.absolute(300))
                                           .width(ProportionValue.absolute(width))
                                           .height(ProportionValue.absolute(height))
                                           .borderSize(borderSize)
                                           .sprite("sprites/ui/ui"))
                /*.element(UIPanel.<LWJGLTexture>builder("play_button")
                                 .anchor(0.5, 0.25)
                                 .origin(0.5, 0.0)
                                 .position(ProportionValue.absolute(0),
                                           ProportionValue.absolute(0))
                                 .size(width, height)
                                 .borderSize(borderSize)
                                 .sprite(sprite, spriteBatch)
                                 .child(UILabel.builder("play_button_label", textRenderer)
                                               .anchor(0.5, 0.5)
                                               .origin(0.5, 0.5)
                                               .position(ProportionValue.absolute(0),
                                                         ProportionValue.percent(0.5))
                                               .text("Play")
                                               .fontSize(24)
                                               .build())
                                 //.onClick((element, event) -> LOG.info("Play clicked!"))
                                 .build())
                .element(UILabel.builder("title_label", textRenderer)
                                .anchor(0.5, 0.25)
                                .origin(0.5, 1.0)
                                .position(ProportionValue.absolute(0),
                                          ProportionValue.absolute(0))
                                .text("Konna")
                                .fontSize(48)
                                .build())*/
                .build();
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        this.camera.useScreenCoordinates();
        this.userInterface.render();
    }

    @Override
    public void close() {
        MemoryUtil.memFree(this.cameraMatricesData);
        glDeleteBuffers(this.screenCameraUbo);
    }
}
