package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.shader;

import fi.jakojaannos.roguelite.engine.view.rendering.shader.ShaderProgram;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindFragDataLocation;
import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;

@Slf4j
class LWJGLShaderProgram implements AutoCloseable, ShaderProgram {
    @Getter private final int shaderProgram;
    private final Collection<Shader> shaders;
    private Map<String, Integer> uniformLocations = new HashMap<>();

    LWJGLShaderProgram(
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

    @Override
    public void use() {
        glUseProgram(this.shaderProgram);
    }

    @Override
    public void bindUniformBlock(final String blockName, final int uniformObjectIndex) {
        int blockIndex = glGetUniformBlockIndex(this.shaderProgram, blockName);
        glUniformBlockBinding(this.shaderProgram, blockIndex, uniformObjectIndex);
    }

    @Override
    public void setUniformMat4x4(final String uniformName, final Matrix4f matrix) {
        try (val stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(this.uniformLocations.computeIfAbsent(uniformName,
                                                                     this::getUniformLocation),
                               false,
                               matrix.get(stack.mallocFloat(16)));
        }
    }

    @Override
    public void setUniform1f(final String uniformName, final float value) {
        glUniform1f(this.uniformLocations.computeIfAbsent(uniformName,
                                                          this::getUniformLocation),
                    value);
    }

    @Override
    public void setUniform2f(final String uniformName, final float a, final float b) {
        glUniform2f(this.uniformLocations.computeIfAbsent(uniformName,
                                                          this::getUniformLocation),
                    a,
                    b);
    }

    public int getUniformLocation(final String name) {
        return glGetUniformLocation(this.shaderProgram, name);
    }

    @Override
    public void close() {
        this.uniformLocations.clear();
        this.shaders.forEach(Shader::close);
        glDeleteProgram(this.shaderProgram);
    }
}
