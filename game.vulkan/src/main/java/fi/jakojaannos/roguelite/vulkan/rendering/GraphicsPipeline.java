package fi.jakojaannos.roguelite.vulkan.rendering;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import fi.jakojaannos.roguelite.util.shader.ShaderCompiler;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

public class GraphicsPipeline implements AutoCloseable {
    private final ByteBuffer compiledVertexShader;
    private final ByteBuffer compiledFragmentShader;

    public GraphicsPipeline(final Path assetRoot) {
        try {
            this.compiledVertexShader = ShaderCompiler.loadGLSLShader(assetRoot.resolve("shaders/vulkan/shader.vert"),
                                                                      VK_SHADER_STAGE_VERTEX_BIT);
            this.compiledFragmentShader = ShaderCompiler.loadGLSLShader(assetRoot.resolve("shaders/vulkan/shader.frag"),
                                                                        VK_SHADER_STAGE_FRAGMENT_BIT);
        } catch (final IOException e) {
            throw new IllegalStateException("Loading shaders failed: " + e);
        }

        // TODO
    }

    @Override
    public void close() {
    }
}
