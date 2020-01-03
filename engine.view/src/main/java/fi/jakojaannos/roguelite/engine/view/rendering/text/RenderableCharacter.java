package fi.jakojaannos.roguelite.engine.view.rendering.text;

import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;

public interface RenderableCharacter {
    double getX0();

    double getX1();

    double getY0();

    double getY1();

    TextureRegion getTextureRegion();
}
