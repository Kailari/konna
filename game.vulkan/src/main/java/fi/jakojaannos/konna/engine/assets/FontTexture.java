package fi.jakojaannos.konna.engine.assets;

import java.nio.FloatBuffer;

import fi.jakojaannos.konna.engine.view.TexturedQuad;
import fi.jakojaannos.konna.engine.vulkan.rendering.ImageView;

public interface FontTexture extends Texture {
    float getPixelHeightScale();

    ImageView getImageView();

    float calculateStringWidthInPixels(String string);

    TexturedQuad getQuadForCharacter(int character, FloatBuffer pX, FloatBuffer pY);
}
