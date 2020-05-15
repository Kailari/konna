package fi.jakojaannos.konna.assets;

import org.joml.Vector4f;

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
