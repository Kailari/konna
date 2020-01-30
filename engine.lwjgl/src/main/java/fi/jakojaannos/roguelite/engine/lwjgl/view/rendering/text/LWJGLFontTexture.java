package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text;

import lombok.Getter;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;
import fi.jakojaannos.roguelite.engine.view.rendering.text.Font;
import fi.jakojaannos.roguelite.engine.view.rendering.text.FontTexture;
import fi.jakojaannos.roguelite.engine.view.rendering.text.RenderableCharacter;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;

public class LWJGLFontTexture implements FontTexture, Texture {
    private static final int FIRST_CHAR = 32;

    @Getter private final float contentScaleX;
    @Getter private final float contentScaleY;
    @Getter private final float pixelHeightScale;

    private final int fontHeight;
    private final LWJGLFont font;
    private final STBTTBakedChar.Buffer bakedCharacters;
    private final STBTTAlignedQuad alignedQuad;

    private final int textureId;
    private final int scaledBitmapW;
    private final int scaledBitmapH;
    private final Map<Integer, TextureRegion> textureRegions = new HashMap<>();

    public LWJGLFontTexture(
            final ByteBuffer ttf,
            final LWJGLFont font,
            final int fontHeight,
            final float contentScaleX,
            final float contentScaleY
    ) {
        this.font = font;
        this.fontHeight = fontHeight;
        this.contentScaleX = contentScaleX;
        this.contentScaleY = contentScaleY;
        this.scaledBitmapW = Math.round(512 * this.contentScaleX);
        this.scaledBitmapH = Math.round(512 * this.contentScaleY);

        this.textureId = glGenTextures();
        this.bakedCharacters = bakeFontToBitmap(ttf);
        this.pixelHeightScale = stbtt_ScaleForPixelHeight(font.getFontInfo(), this.fontHeight);
        this.alignedQuad = STBTTAlignedQuad.malloc();
    }

    private STBTTBakedChar.Buffer bakeFontToBitmap(final ByteBuffer ttf) {
        final var cdata = STBTTBakedChar.malloc(96); // 96 ???
        final var bitmap = BufferUtils.createByteBuffer(this.scaledBitmapW * this.scaledBitmapH);
        glBindTexture(GL_TEXTURE_2D, this.textureId);
        stbtt_BakeFontBitmap(ttf,
                             this.fontHeight * this.contentScaleY,
                             bitmap,
                             this.scaledBitmapW,
                             this.scaledBitmapH,
                             FIRST_CHAR,
                             cdata);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D,
                     0,
                     GL_RED,
                     this.scaledBitmapW,
                     this.scaledBitmapH,
                     0,
                     GL_RED,
                     GL_UNSIGNED_BYTE,
                     bitmap);

        return cdata;
    }

    @Override
    public int getWidth() {
        return this.scaledBitmapW;
    }

    @Override
    public int getHeight() {
        return this.scaledBitmapH;
    }

    @Override
    public void use() {
        glBindTexture(GL_TEXTURE_2D, this.textureId);
    }

    @Override
    public RenderableCharacter getNextCharacterAndAdvance(
            final int codePoint,
            final IntBuffer pCodePoint,
            final FloatBuffer pX,
            final FloatBuffer pY,
            final int i,
            final int to,
            final String string,
            final float factorX
    ) {
        final var cpX = pX.get(0);
        this.alignedQuad.clear();
        stbtt_GetBakedQuad(this.bakedCharacters,
                           this.scaledBitmapW,
                           this.scaledBitmapH,
                           codePoint - FIRST_CHAR,
                           pX,
                           pY,
                           this.alignedQuad,
                           true);
        pX.put(0, (float) scale(cpX, pX.get(0), factorX));
        if (this.font.isKerningEnabled() && i < to) {
            Font.getCP(string, to, i, pCodePoint);
            final int kernAdvance = stbtt_GetCodepointKernAdvance(this.font.getFontInfo(),
                                                                  codePoint,
                                                                  pCodePoint.get(0));
            pX.put(0, pX.get(0) + kernAdvance * this.pixelHeightScale);
        }

        return new LWJGLRenderableCharacter(this.alignedQuad.x0(), this.alignedQuad.x1(),
                                            this.alignedQuad.y0(), this.alignedQuad.y1(),
                                            this.textureRegions
                                                    .computeIfAbsent(codePoint,
                                                                     key -> new TextureRegion(this,
                                                                                              this.alignedQuad.s0(),
                                                                                              this.alignedQuad.t0(),
                                                                                              this.alignedQuad.s1(),
                                                                                              this.alignedQuad.t1())));
    }

    private double scale(
            final double center,
            final double offset,
            final double factor
    ) {
        return (offset - center) * factor + center;
    }

    @Override
    public void close() {
        this.bakedCharacters.close();
        this.alignedQuad.close();
        glDeleteTextures(this.textureId);
    }
}
