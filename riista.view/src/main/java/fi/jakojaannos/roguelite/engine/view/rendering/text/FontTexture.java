package fi.jakojaannos.roguelite.engine.view.rendering.text;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public interface FontTexture {
    void use();

    float getPixelHeightScale();

    float getContentScaleX();

    float getContentScaleY();

    RenderableCharacter getNextCharacterAndAdvance(
            int codePoint,
            IntBuffer pCodePoint,
            FloatBuffer pX,
            FloatBuffer pY,
            int i,
            int to,
            String string,
            float factorX
    );
}
