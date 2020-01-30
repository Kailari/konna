package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.shader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.jakojaannos.roguelite.engine.view.rendering.shader.ShaderBuilder;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.ShaderProgram;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

public class LWJGLShaderBuilder implements ShaderBuilder {
    private final int programPtr;
    private final List<Shader> shaders = new ArrayList<>();
    private final Map<Integer, String> attributeLocations = new HashMap<>();
    private final Map<Integer, String> fragmentDataLocations = new HashMap<>();

    public LWJGLShaderBuilder() {
        this.programPtr = glCreateProgram();
    }

    @Override
    public ShaderBuilder vertexShader(final Path sourcePath) {
        return shader(sourcePath, GL_VERTEX_SHADER);
    }

    @Override
    public ShaderBuilder fragmentShader(final Path sourcePath) {
        return shader(sourcePath, GL_FRAGMENT_SHADER);
    }

    @Override
    public ShaderBuilder geometryShader(final Path sourcePath) {
        return shader(sourcePath, GL_GEOMETRY_SHADER);
    }

    @Override
    public ShaderBuilder shader(final Path sourcePath, final int shaderType) {
        this.shaders.add(new Shader(this.programPtr, sourcePath, shaderType));
        return this;
    }

    @Override
    public ShaderBuilder attributeLocation(final int index, final String name) {
        this.attributeLocations.put(index, name);
        return this;
    }

    @Override
    public ShaderBuilder fragmentDataLocation(final int index, final String name) {
        this.fragmentDataLocations.put(index, name);
        return this;
    }

    @Override
    public ShaderProgram build() {
        return new LWJGLShaderProgram(this.programPtr,
                                      this.shaders,
                                      this.attributeLocations,
                                      this.fragmentDataLocations);
    }
}
