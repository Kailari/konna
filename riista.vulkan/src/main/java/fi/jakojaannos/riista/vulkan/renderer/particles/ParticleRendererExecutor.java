package fi.jakojaannos.riista.vulkan.renderer.particles;

import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.vulkan.CameraDescriptor;
import fi.jakojaannos.riista.vulkan.application.PresentableState;
import fi.jakojaannos.riista.vulkan.internal.RenderingBackend;
import fi.jakojaannos.riista.vulkan.internal.command.CommandBuffer;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorSetLayout;
import fi.jakojaannos.riista.vulkan.internal.types.VkPrimitiveTopology;
import fi.jakojaannos.riista.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.riista.vulkan.rendering.RenderPass;
import fi.jakojaannos.riista.vulkan.rendering.RenderSubpass;
import fi.jakojaannos.riista.vulkan.util.RecreateCloseable;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class ParticleRendererExecutor extends RecreateCloseable {
    private final GraphicsPipeline<ParticleVertex> pipeline;

    public ParticleRendererExecutor(
            final RenderingBackend backend,
            final RenderPass renderPass,
            final RenderSubpass mainRenderPass,
            final AssetManager assetManager,
            final DescriptorSetLayout cameraDescriptorLayout
    ) {
        this.pipeline = new GraphicsPipeline<>(backend,
                                               renderPass,
                                               mainRenderPass,
                                               assetManager,
                                               "shaders/vulkan/particle.vert",
                                               "shaders/vulkan/particle.frag",
                                               VkPrimitiveTopology.POINT_LIST,
                                               ParticleVertex.FORMAT,
                                               cameraDescriptorLayout);
    }

    public void flush(
            final PresentableState state,
            final CameraDescriptor cameraUBO,
            final CommandBuffer commandBuffer,
            final int imageIndex
    ) {
        try (final var stack = stackPush()) {
            final var pushConstantData = stack.malloc(3 * Float.BYTES);

            vkCmdBindPipeline(commandBuffer.getHandle(),
                              VK_PIPELINE_BIND_POINT_GRAPHICS,
                              this.pipeline.getHandle());

            vkCmdBindDescriptorSets(commandBuffer.getHandle(),
                                    VK_PIPELINE_BIND_POINT_GRAPHICS,
                                    this.pipeline.getLayout(),
                                    0,
                                    stack.longs(cameraUBO.getDescriptorSet(imageIndex)),
                                    null);

            for (final var particleSystem : state.particleSystemEntries()) {
                particleSystem.position.get(0, pushConstantData);
                vkCmdPushConstants(commandBuffer.getHandle(),
                                   this.pipeline.getLayout(),
                                   VK_SHADER_STAGE_VERTEX_BIT,
                                   0,
                                   pushConstantData);

                vkCmdDraw(commandBuffer.getHandle(),
                          particleSystem.count,
                          1,
                          0,
                          0);
            }
        }
    }

    @Override
    protected void recreate() {
        this.pipeline.tryRecreate();
    }

    @Override
    protected void cleanup() {
    }

    @Override
    public void close() {
        super.close();

        this.pipeline.close();
    }
}
