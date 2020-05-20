package fi.jakojaannos.konna.engine.assets.material;

import org.joml.Vector4f;

import javax.annotation.Nullable;

import fi.jakojaannos.konna.engine.assets.Material;
import fi.jakojaannos.konna.engine.assets.Texture;

public record MaterialImpl(
        Vector4f ambient,
        Vector4f diffuse,
        Vector4f specular,
        @Nullable Texture texture,
        float reflectance
) implements Material {
    public static final Vector4f DEFAULT_COLOR = new Vector4f(1.0f);
}
