package fi.jakojaannos.roguelite.engine.view.rendering.shader;

import java.nio.file.Path;

public interface ShaderBuilder {
    ShaderBuilder vertexShader(Path sourcePath);

    ShaderBuilder fragmentShader(Path sourcePath);

    ShaderBuilder geometryShader(Path sourcePath);

    ShaderBuilder shader(Path sourcePath, int shaderType);

    ShaderBuilder attributeLocation(int index, String name);

    ShaderBuilder fragmentDataLocation(int index, String name);

    ShaderProgram build();
}
