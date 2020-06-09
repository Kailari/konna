package fi.jakojaannos.riista.vulkan.assets.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Optional;

import fi.jakojaannos.riista.assets.AssetLoader;
import fi.jakojaannos.konna.engine.util.shader.ShaderCompiler;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

public class ShaderLoader implements AssetLoader<Shader> {
    private static final Logger LOG = LoggerFactory.getLogger(ShaderLoader.class);

    @Override
    public Optional<Shader> load(final Path path) {
        final ByteBuffer compiled;
        try {
            final var extension = getFileExtension(path);

            final var isVertex = extension.equalsIgnoreCase(".vert");
            final var isFrag = extension.equalsIgnoreCase(".frag");
            final int stage;
            if (isVertex) {
                stage = VK_SHADER_STAGE_VERTEX_BIT;
            } else if (isFrag) {
                stage = VK_SHADER_STAGE_FRAGMENT_BIT;
            } else {


                throw new IllegalStateException("Unknown shader file type: " + extension);
            }

            compiled = ShaderCompiler.loadGLSLShader(path, stage);
        } catch (final IOException e) {
            LOG.error("Loading shaders failed:", e);
            return Optional.empty();
        }

        return Optional.of(new Shader(compiled));
    }

    private static String getFileExtension(final Path path) {
        final var fileName = path.getFileName().toString();
        final var extensionStart = fileName.lastIndexOf('.');

        return fileName.substring(extensionStart);
    }
}
