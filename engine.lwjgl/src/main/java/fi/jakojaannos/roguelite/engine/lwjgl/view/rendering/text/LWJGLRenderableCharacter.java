package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text;

import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;
import fi.jakojaannos.roguelite.engine.view.rendering.text.RenderableCharacter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LWJGLRenderableCharacter implements RenderableCharacter {
    @Getter private final double x0, x1, y0, y1;
    @Getter private final TextureRegion textureRegion;
}
