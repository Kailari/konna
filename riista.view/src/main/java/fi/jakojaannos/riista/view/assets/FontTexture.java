package fi.jakojaannos.riista.view.assets;

import java.nio.FloatBuffer;

import fi.jakojaannos.riista.view.TexturedQuad;

public interface FontTexture extends Texture {
    float getPixelHeightScale();

    float calculateStringWidthInPixels(String string);

    TexturedQuad getQuadForCharacter(int character, FloatBuffer pX, FloatBuffer pY);

    int getFontSize();
}
