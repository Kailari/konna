package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text;

import fi.jakojaannos.roguelite.engine.view.rendering.text.FontTexture;
import lombok.Getter;
import lombok.val;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;

public class LWJGLFontTexture implements AutoCloseable, FontTexture {
    private static final int FIRST_CHAR = 32;

    @Getter private final float contentScaleX, contentScaleY;
    @Getter private final float pixelHeightScale;

    private final int fontHeight;
    private final STBTTBakedChar.Buffer bakedCharacters;

    private final int textureId;
    private final int scaledBitmapW, scaledBitmapH;

    public LWJGLFontTexture(
            final ByteBuffer ttf,
            final STBTTFontinfo fontInfo,
            final int fontHeight,
            final float contentScaleX,
            final float contentScaleY
    ) {
        this.fontHeight = fontHeight;
        this.contentScaleX = contentScaleX;
        this.contentScaleY = contentScaleY;
        this.scaledBitmapW = Math.round(512 * this.contentScaleX);
        this.scaledBitmapH = Math.round(512 * this.contentScaleY);

        this.textureId = glGenTextures();
        this.bakedCharacters = bakeFontToBitmap(ttf);
        this.pixelHeightScale = stbtt_ScaleForPixelHeight(fontInfo, this.fontHeight);
    }

    private STBTTBakedChar.Buffer bakeFontToBitmap(ByteBuffer ttf) {
        val cdata = STBTTBakedChar.malloc(96); // 96 ???
        val bitmap = BufferUtils.createByteBuffer(this.scaledBitmapW * this.scaledBitmapH);
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
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, this.scaledBitmapW, this.scaledBitmapH, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);

        return cdata;
    }

    @Override
    public void use() {
        glBindTexture(GL_TEXTURE_2D, this.textureId);
    }

    public void getNextCharacterToQuad(
            final int codePoint,
            final FloatBuffer pX,
            final FloatBuffer pY,
            final STBTTAlignedQuad alignedQuad
    ) {
        stbtt_GetBakedQuad(this.bakedCharacters,
                           this.scaledBitmapW,
                           this.scaledBitmapH,
                           codePoint - FIRST_CHAR,
                           pX,
                           pY,
                           alignedQuad,
                           true);
    }

    @Override
    public void close() {
        this.bakedCharacters.close();
        glDeleteTextures(this.textureId);
    }
}
