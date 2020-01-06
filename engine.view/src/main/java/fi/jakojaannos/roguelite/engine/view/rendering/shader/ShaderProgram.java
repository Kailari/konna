package fi.jakojaannos.roguelite.engine.view.rendering.shader;

import org.joml.Matrix4f;
import org.joml.Vector2f;

public interface ShaderProgram extends AutoCloseable {
    void use();

    void bindUniformBlock(String blockName, int uniformObjectIndex);

    void setUniformMat4x4(String uniformName, Matrix4f matrix);

    void setUniform1f(String uniformName, float value);

    void setUniform2f(String uniformName, float a, float b);

    default void setUniform2f(String uniformName, Vector2f value) {
        setUniform2f(uniformName, value.x(), value.y());
    }
}
