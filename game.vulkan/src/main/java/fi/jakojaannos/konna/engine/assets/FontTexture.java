package fi.jakojaannos.konna.engine.assets;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public interface FontTexture extends AutoCloseable {
    float getPixelHeightScale();

    float calculateStringWidthInPixels(String string);

    // FIXME: Move to impl or sth, this is an implementation detail and thus should not be here.
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

    @Override
    void close();
}
