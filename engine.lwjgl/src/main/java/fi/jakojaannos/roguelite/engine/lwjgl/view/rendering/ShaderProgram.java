package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.util.Collection;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindFragDataLocation;

@Slf4j
public class ShaderProgram implements AutoCloseable {
    @Getter private final int shaderProgram;
    private final Collection<Shader> shaders;

    public static ShaderBuilder builder() {
        return new ShaderBuilder();
    }

    ShaderProgram(
            final int programPtr,
            final Collection<Shader> shaders,
            final Map<Integer, String> attributeLocations,
            final Map<Integer, String> fragDataLocations
    ) {
        this.shaderProgram = programPtr;
        this.shaders = shaders;

        attributeLocations.forEach((index, name) -> glBindAttribLocation(this.shaderProgram, index, name));
        fragDataLocations.forEach((colorNumber, name) -> glBindFragDataLocation(this.shaderProgram, colorNumber, name));

        // Link the program and check for errors
        glLinkProgram(this.shaderProgram);
        if (glGetProgrami(this.shaderProgram, GL_LINK_STATUS) != GL_TRUE) {
            LOG.error(glGetProgramInfoLog(this.shaderProgram));
        }
    }

    public void use() {
        glUseProgram(this.shaderProgram);
    }

    public void setUniformMat4x4(final int uniformLocation, final Matrix4f matrix) {
        try (val stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(uniformLocation, false, matrix.get(stack.mallocFloat(16)));
        }
    }

    public void setUniform1f(final int uniformLocation, final float value) {
        glUniform1f(uniformLocation, value);
    }

    public int getUniformLocation(final String name) {
        return glGetUniformLocation(shaderProgram, name);
    }

    @Override
    public void close() {
        this.shaders.forEach(Shader::close);

        glDeleteProgram(this.shaderProgram);
    }
}
