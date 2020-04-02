package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.shader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER;

public class Shader implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(Shader.class);

    private final int shaderPtr;

    public Shader(final int programPtr, final Path sourcePath, final int shaderType) {
        this.shaderPtr = glCreateShader(shaderType);

        try {
            glShaderSource(this.shaderPtr, Files.readString(sourcePath));
        } catch (final IOException e) {
            LOG.error("Loading shader \"{}\" failed!", sourcePath);
            return;
        }
        glCompileShader(this.shaderPtr);

        if (glGetShaderi(this.shaderPtr, GL_COMPILE_STATUS) != GL_TRUE) {
            LOG.error("Error compiling shader ({}):\n{}",
                      shaderTypeToString(shaderType),
                      glGetShaderInfoLog(this.shaderPtr));
        }

        glAttachShader(programPtr, this.shaderPtr);
    }

    @Override
    public void close() {
        glDeleteShader(this.shaderPtr);
    }

    private static String shaderTypeToString(final int shaderType) {
        return switch (shaderType) {
            case GL_GEOMETRY_SHADER -> "GL_GEOMETRY_SHADER";
            case GL_VERTEX_SHADER -> "GL_VERTEX_SHADER";
            case GL_FRAGMENT_SHADER -> "GL_FRAGMENT_SHADER";
            case GL_COMPUTE_SHADER -> "GL_COMPUTE_SHADER";
            default -> "UNKNOWN_SHADER_TYPE";
        };
    }
}
