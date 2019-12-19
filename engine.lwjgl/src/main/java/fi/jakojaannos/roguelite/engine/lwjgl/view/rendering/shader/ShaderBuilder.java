package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.shader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class ShaderBuilder {
    private final int programPtr;
    private final List<Shader> shaders = new ArrayList<>();
    private final Map<Integer, String> attributeLocations = new HashMap<>();
    private final Map<Integer, String> fragmentDataLocations = new HashMap<>();

    ShaderBuilder() {
        this.programPtr = glCreateProgram();
    }

    public ShaderBuilder vertexShader(final Path sourcePath) {
        return shader(sourcePath, GL_VERTEX_SHADER);
    }

    public ShaderBuilder fragmentShader(final Path sourcePath) {
        return shader(sourcePath, GL_FRAGMENT_SHADER);
    }

    public ShaderBuilder shader(final Path sourcePath, final int shaderType) {
        this.shaders.add(new Shader(this.programPtr, sourcePath, shaderType));
        return this;
    }

    public ShaderBuilder attributeLocation(final int index, final String name) {
        this.attributeLocations.put(index, name);
        return this;
    }

    public ShaderBuilder fragmentDataLocation(final int index, final String name) {
        this.fragmentDataLocations.put(index, name);
        return this;
    }

    public ShaderProgram build() {
        return new ShaderProgram(this.programPtr,
                                 this.shaders,
                                 this.attributeLocations,
                                 this.fragmentDataLocations);
    }
}
