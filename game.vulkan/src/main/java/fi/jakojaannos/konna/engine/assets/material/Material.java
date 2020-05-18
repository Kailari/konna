package fi.jakojaannos.konna.engine.assets.material;

import org.joml.Vector4f;

import fi.jakojaannos.konna.engine.assets.texture.Texture;

public record Material(
        Vector4f ambient,
        Vector4f diffuse,
        Vector4f specular,
        Texture texture,
        boolean hasTexture,
        float reflectance
) {
    public static final Vector4f DEFAULT_COLOR = new Vector4f(1.0f);
}
