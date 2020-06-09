package fi.jakojaannos.roguelite.engine.view.rendering;

public record TextureRegion(
        Texture texture,
        double u0,
        double v0,
        double u1,
        double v1
) {
}
