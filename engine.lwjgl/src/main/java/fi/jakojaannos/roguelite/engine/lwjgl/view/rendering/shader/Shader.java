package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.shader;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER;

@Slf4j
public class Shader implements AutoCloseable {
    private final int shaderPtr;

    public Shader(final int programPtr, final Path sourcePath, final int shaderType) {
        this.shaderPtr = glCreateShader(shaderType);

        try {
            glShaderSource(this.shaderPtr, Files.readString(sourcePath));
        } catch (IOException e) {
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

    private static String shaderTypeToString(int shaderType) {
        switch (shaderType) {
            case GL_GEOMETRY_SHADER:
                return "GL_GEOMETRY_SHADER";
            case GL_VERTEX_SHADER:
                return "GL_VERTEX_SHADER";
            case GL_FRAGMENT_SHADER:
                return "GL_FRAGMENT_SHADER";
            case GL_COMPUTE_SHADER:
                return "GL_COMPUTE_SHADER";
            default:
                return "UNKNOWN_SHADER_TYPE";
        }
    }

    @Override
    public void close() {
        glDeleteShader(this.shaderPtr);
    }
}
