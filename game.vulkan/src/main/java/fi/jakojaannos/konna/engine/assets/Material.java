package fi.jakojaannos.konna.engine.assets;

import org.joml.Vector4f;

import javax.annotation.Nullable;

public interface Material {
    Vector4f ambient();

    Vector4f diffuse();

    Vector4f specular();

    float reflectance();

    @Nullable
    Texture texture();
}
