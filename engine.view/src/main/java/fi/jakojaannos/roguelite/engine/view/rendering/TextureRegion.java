package fi.jakojaannos.roguelite.engine.view.rendering;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TextureRegion {
    @Getter private final Texture texture;
    @Getter private final double u0;
    @Getter private final double v0;
    @Getter private final double u1;
    @Getter private final double v1;
}
