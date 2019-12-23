package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.LWJGLTexture;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.TextRenderer;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import lombok.val;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

public class MainMenuRenderingSystem implements ECSSystem, AutoCloseable {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
    }

    private final TextRenderer textRenderer;
    private final LWJGLCamera camera;
    private final SpriteRegistry<LWJGLTexture> spriteRegistry;
    private final SpriteBatch<LWJGLTexture, LWJGLCamera> spriteBatch;

    private final int screenCameraUbo;
    private final ByteBuffer cameraMatricesData;

    public MainMenuRenderingSystem(
            TextRenderer textRenderer,
            LWJGLCamera camera,
            SpriteRegistry<LWJGLTexture> spriteRegistry,
            SpriteBatch<LWJGLTexture, LWJGLCamera> spriteBatch
    ) {
        this.textRenderer = textRenderer;
        this.camera = camera;
        this.spriteRegistry = spriteRegistry;
        this.spriteBatch = spriteBatch;

        this.screenCameraUbo = glGenBuffers();
        this.cameraMatricesData = MemoryUtil.memAlloc(2 * 16 * 4);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val halfScreenWidth = this.camera.getViewportWidthInPixels() / 2.0;
        this.textRenderer.drawCenteredOnScreen(halfScreenWidth, 50, 48, "Konna");

        val sprite = this.spriteRegistry.getByAssetName("sprites/ui/ui");

        val y = 150;
        val width = 600;
        val height = 300;
        val xOffset = -width / 2.0;
        val x = halfScreenWidth + xOffset;
        val borderSize = 50;

        this.camera.useScreenCoordinates();
        this.spriteBatch.begin();

        this.spriteBatch.draw(sprite, "panel_top_left", 0, x, y, borderSize, borderSize);
        this.spriteBatch.draw(sprite, "panel_top", 0, x + borderSize, y, width - 2 * borderSize, borderSize);
        this.spriteBatch.draw(sprite, "panel_top_right", 0, x + width - borderSize, y, borderSize, borderSize);

        this.spriteBatch.draw(sprite, "panel_left", 0, x, y + borderSize, borderSize, height - 2 * borderSize);
        this.spriteBatch.draw(sprite, "panel_fill", 0, x + borderSize, y + borderSize, width - 2 * borderSize, height - 2 * borderSize);
        this.spriteBatch.draw(sprite, "panel_right", 0, x + width - borderSize, y + borderSize, borderSize, height - 2 * borderSize);

        this.spriteBatch.draw(sprite, "panel_bottom_left", 0, x, y + height - borderSize, borderSize, borderSize);
        this.spriteBatch.draw(sprite, "panel_bottom", 0, x + borderSize, y + height - borderSize, width - 2 * borderSize, borderSize);
        this.spriteBatch.draw(sprite, "panel_bottom_right", 0, x + width - borderSize, y + height - borderSize, borderSize, borderSize);

        this.spriteBatch.end();
    }

    @Override
    public void close() {
        MemoryUtil.memFree(this.cameraMatricesData);
        glDeleteBuffers(this.screenCameraUbo);
    }
}
