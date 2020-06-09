package fi.jakojaannos.roguelite.engine.view.rendering.text;

import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;

public record RenderableCharacter(
        double x0,
        double x1,
        double y0,
        double y1,
        TextureRegion textureRegion
) {
}
