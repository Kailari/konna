package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;
import fi.jakojaannos.roguelite.engine.view.rendering.text.RenderableCharacter;

@RequiredArgsConstructor
public class LWJGLRenderableCharacter implements RenderableCharacter {
    @Getter private final double x0;
    @Getter private final double x1;
    @Getter private final double y0;
    @Getter private final double y1;
    @Getter private final TextureRegion textureRegion;
}
