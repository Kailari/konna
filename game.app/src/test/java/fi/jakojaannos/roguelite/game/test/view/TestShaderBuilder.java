package fi.jakojaannos.roguelite.game.test.view;

import fi.jakojaannos.roguelite.engine.view.rendering.shader.ShaderBuilder;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.ShaderProgram;

import java.nio.file.Path;

import static org.mockito.Mockito.mock;

public class TestShaderBuilder implements ShaderBuilder {
    @Override
    public ShaderBuilder vertexShader(final Path sourcePath) {
        return this;
    }

    @Override
    public ShaderBuilder fragmentShader(final Path sourcePath) {
        return this;
    }

    @Override
    public ShaderBuilder shader(final Path sourcePath, final int shaderType) {
        return this;
    }

    @Override
    public ShaderBuilder attributeLocation(final int index, final String name) {
        return this;
    }

    @Override
    public ShaderBuilder fragmentDataLocation(final int index, final String name) {
        return this;
    }

    @Override
    public ShaderProgram build() {
        return mock(ShaderProgram.class);
    }
}
