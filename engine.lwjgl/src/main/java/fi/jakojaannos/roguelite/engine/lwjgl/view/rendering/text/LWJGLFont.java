package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import fi.jakojaannos.roguelite.engine.view.rendering.text.Font;

import static org.lwjgl.stb.STBTruetype.*;

public class LWJGLFont implements Font {
    private final float contentScaleX;
    private final float contentScaleY;

    private final ByteBuffer ttf;
    private final STBTTFontinfo fontInfo;
    private final Map<Integer, LWJGLFontTexture> sizes = new HashMap<>();
    private final int ascent;
    private final int descent;
    private final int lineGap;

    public STBTTFontinfo getFontInfo() {
        return this.fontInfo;
    }

    @Override
    public boolean isKerningEnabled() {
        return false;
    }

    @Override
    public float getLineOffset() {
        return this.ascent - this.descent + this.lineGap;
    }

    public LWJGLFont(
            final Path assetPath,
            final float contentScaleX,
            final float contentScaleY
    ) {
        this.contentScaleX = contentScaleX;
        this.contentScaleY = contentScaleY;

        try (final var fc = Files.newByteChannel(assetPath)) {
            this.ttf = BufferUtils.createByteBuffer((int) fc.size() + 1);
            //noinspection StatementWithEmptyBody
            while (fc.read(this.ttf) != -1) ;
            this.ttf.flip();
        } catch (final IOException e) {
            throw new IllegalStateException(String.format("Could not load font from %s!", assetPath.toString()));
        }

        this.fontInfo = STBTTFontinfo.create();
        if (!stbtt_InitFont(this.fontInfo, this.ttf)) {
            throw new IllegalStateException("Failed to initialize font descriptor.");
        }

        try (final var stack = MemoryStack.stackPush()) {
            final var pAscent = stack.mallocInt(1);
            final var pDescent = stack.mallocInt(1);
            final var pLineGap = stack.mallocInt(1);

            stbtt_GetFontVMetrics(this.fontInfo, pAscent, pDescent, pLineGap);
            this.ascent = pAscent.get(0);
            this.descent = pDescent.get(0);
            this.lineGap = pLineGap.get(0);
        }
    }

    public LWJGLFontTexture getTextureForSize(final int fontSize) {
        return this.sizes.computeIfAbsent(fontSize,
                                          key -> new LWJGLFontTexture(this.ttf,
                                                                      this,
                                                                      key,
                                                                      this.contentScaleX,
                                                                      this.contentScaleY));
    }

    @Override
    public double getStringWidthInPixels(final int fontSize, final String string) {
        int width = 0;
        try (final var stack = MemoryStack.stackPush()) {
            final var pCodePoint = stack.mallocInt(1);
            final var pAdvancedWidth = stack.mallocInt(1);
            final var pLeftSideBearing = stack.mallocInt(1);

            final var from = 0;
            final var to = string.length();
            int i = from;
            while (i < to) {
                i += Font.getCP(string, to, i, pCodePoint);
                final var cp = pCodePoint.get(0);

                stbtt_GetCodepointHMetrics(this.fontInfo, cp, pAdvancedWidth, pLeftSideBearing);
                width += pAdvancedWidth.get(0);

                if (isKerningEnabled() && i < to) {
                    Font.getCP(string, to, i, pCodePoint);
                    width += stbtt_GetCodepointKernAdvance(this.fontInfo, cp, pCodePoint.get(0));
                }
            }
        }

        return width * stbtt_ScaleForPixelHeight(this.fontInfo, fontSize);
    }

    @Override
    public void close() {
        this.sizes.values().forEach(LWJGLFontTexture::close);
    }
}
