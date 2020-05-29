package fi.jakojaannos.konna.engine.assets;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import fi.jakojaannos.konna.engine.vulkan.rendering.ImageView;

public interface FontTexture extends Texture {
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

    ImageView getImageView();
}
