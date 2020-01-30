package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text;

import lombok.extern.slf4j.Slf4j;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.text.Font;
import fi.jakojaannos.roguelite.engine.view.rendering.text.RenderableCharacter;
import fi.jakojaannos.roguelite.engine.view.rendering.text.TextRenderer;

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
        final var textWidth = font.getStringWidthInPixels(fontSize, text);

        final var textX = x - textWidth / 2.0;
        final var textY = y - fontSize / 2.0;
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
        final var fontTexture = font.getTextureForSize(fontSize);
        final var fontPixelHeightScale = fontTexture.getPixelHeightScale();

        this.spriteBatch.begin();
        fontTexture.use();
        try (final var stack = MemoryStack.stackPush()) {
            final var pCodePoint = stack.mallocInt(1);
            final var pX = stack.floats(0.0f);
            final var pY = stack.floats(0.0f);

            final var factorX = 1.0f / fontTexture.getContentScaleX();
            final var factorY = 1.0f / fontTexture.getContentScaleY();

            var lineY = 0.0f;

            for (int i = 0, to = string.length(); i < to; ) {
                i += Font.getCP(string, to, i, pCodePoint);

                final var codePoint = pCodePoint.get(0);
                if (codePoint == '\n') {
                    pX.put(0, 0.0f);

                    final var lineOffset = font.getLineOffset() * fontPixelHeightScale;
                    final var nextLineY = pY.get(0) + lineOffset;
                    pY.put(0, nextLineY);
                    lineY = nextLineY;

                    continue;
                } else if (codePoint < 32 || codePoint >= 128) {
                    continue;
                }

                final var cpX = pX.get(0);
                RenderableCharacter renderableCharacter = fontTexture.getNextCharacterAndAdvance(codePoint,
                                                                                                 pCodePoint,
                                                                                                 pX, pY,
                                                                                                 i, to,
                                                                                                 string,
                                                                                                 factorX);
                final var x0 = x + scale(cpX, renderableCharacter.getX0(), factorX);
                final var x1 = x + scale(cpX, renderableCharacter.getX1(), factorX);
                final var y0 = y + fontSize + scale(lineY, renderableCharacter.getY0(), factorY);
                final var y1 = y + fontSize + scale(lineY, renderableCharacter.getY1(), factorY);

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
