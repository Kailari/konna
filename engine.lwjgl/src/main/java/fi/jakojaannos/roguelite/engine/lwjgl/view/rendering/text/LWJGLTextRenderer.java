package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text;

import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.text.Font;
import fi.jakojaannos.roguelite.engine.view.rendering.text.RenderableCharacter;
import fi.jakojaannos.roguelite.engine.view.rendering.text.TextRenderer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Path;

@Slf4j
public class LWJGLTextRenderer implements TextRenderer {
    private static final int SIZE_IN_BYTES = (2 + 2 + 3) * 4;

    private final ByteBuffer vertexDataBuffer;
    private final SpriteBatch spriteBatch;

    public LWJGLTextRenderer(
            final Path assetRoot,
            final RenderingBackend backend
    ) {
        this.spriteBatch = backend.createSpriteBatch(assetRoot, "text");
        this.vertexDataBuffer = MemoryUtil.memAlloc(4 * SIZE_IN_BYTES);
    }

    @Override
    public void drawCentered(
            final double x,
            final double y,
            final int fontSize,
            final Font font,
            final String text
    ) {
        val textWidth = font.getStringWidthInPixels(fontSize, text);

        val textX = x - textWidth / 2.0;
        val textY = y - fontSize / 2.0;
        draw(textX, textY, fontSize, font, text);
    }

    @Override
    public void draw(
            final double x,
            final double y,
            final int fontSize,
            final Font font,
            final String string
    ) {
        val fontTexture = font.getTextureForSize(fontSize);
        val fontPixelHeightScale = fontTexture.getPixelHeightScale();

        this.spriteBatch.begin();
        fontTexture.use();
        try (val stack = MemoryStack.stackPush()) {
            val pCodePoint = stack.mallocInt(1);
            val pX = stack.floats(0.0f);
            val pY = stack.floats(0.0f);

            val factorX = 1.0f / fontTexture.getContentScaleX();
            val factorY = 1.0f / fontTexture.getContentScaleY();

            var lineY = 0.0f;

            for (int i = 0, to = string.length(); i < to; ) {
                i += Font.getCP(string, to, i, pCodePoint);

                val codePoint = pCodePoint.get(0);
                if (codePoint == '\n') {
                    pX.put(0, 0.0f);

                    val lineOffset = font.getLineOffset() * fontPixelHeightScale;
                    val nextLineY = pY.get(0) + lineOffset;
                    pY.put(0, nextLineY);
                    lineY = nextLineY;

                    continue;
                } else if (codePoint < 32 || codePoint >= 128) {
                    continue;
                }

                val cpX = pX.get(0);
                RenderableCharacter renderableCharacter = fontTexture.getNextCharacterAndAdvance(codePoint, pCodePoint, pX, pY, i, to, string, factorX);
                val x0 = x + scale(cpX, renderableCharacter.getX0(), factorX);
                val x1 = x + scale(cpX, renderableCharacter.getX1(), factorX);
                val y0 = y + fontSize + scale(lineY, renderableCharacter.getY0(), factorY);
                val y1 = y + fontSize + scale(lineY, renderableCharacter.getY1(), factorY);

                this.spriteBatch.draw(renderableCharacter.getTextureRegion(),
                                      x0, y0, x1, y1,
                                      1.0, 1.0, 1.0);
            }
        }
        this.spriteBatch.end();
    }

    private double scale(
            final double center,
            final double offset,
            final double factor
    ) {
        return (offset - center) * factor + center;
    }

    @Override
    public void close() throws Exception {
        MemoryUtil.memFree(this.vertexDataBuffer);
        this.spriteBatch.close();
    }
}
