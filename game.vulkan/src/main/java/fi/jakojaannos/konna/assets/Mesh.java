package fi.jakojaannos.konna.assets;

import fi.jakojaannos.konna.MaterialInstance;
import fi.jakojaannos.konna.util.RecreateCloseable;
import fi.jakojaannos.konna.vulkan.GPUMesh;
import fi.jakojaannos.konna.vulkan.TextureSampler;
import fi.jakojaannos.konna.vulkan.VertexFormat;
import fi.jakojaannos.konna.vulkan.command.CommandBuffer;
import fi.jakojaannos.konna.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.konna.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.konna.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.konna.vulkan.rendering.Swapchain;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class Mesh<TVertex> extends RecreateCloseable {
    private final GPUMesh<TVertex> gpuMesh;
    private final MaterialInstance material;

    @Override
    protected boolean isRecreateRequired() {
        return true;
    }

    public Mesh(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout,
            final TextureSampler sampler,
            final VertexFormat<TVertex> format,
            final TVertex[] vertices,
            final Integer[] indices,
            final Material material
    ) {
        this.material = new MaterialInstance(deviceContext,
                                             swapchain,
                                             descriptorPool,
                                             layout,
                                             sampler,
                                             material);
        this.gpuMesh = new GPUMesh<>(deviceContext, format, vertices, indices);
    }

    public void draw(
            final GraphicsPipeline<TVertex> pipeline,
            final CommandBuffer commandBuffer,
            final int imageIndex
    ) {
        try (final var stack = stackPush()) {
            vkCmdBindDescriptorSets(commandBuffer.getHandle(),
                                    VK_PIPELINE_BIND_POINT_GRAPHICS,
                                    pipeline.getLayout(),
                                    2,
                                    stack.longs(this.material.getDescriptorSet(imageIndex)),
                                    null);

            vkCmdBindVertexBuffers(commandBuffer.getHandle(),
                                   0,
                                   stack.longs(this.gpuMesh.getVertexBuffer().getHandle()),
                                   stack.longs(0L));
            vkCmdBindIndexBuffer(commandBuffer.getHandle(),
                                 this.gpuMesh.getIndexBuffer().getHandle(),
                                 0,
                                 VK_INDEX_TYPE_UINT32);

            vkCmdDrawIndexed(commandBuffer.getHandle(),
                             this.gpuMesh.getIndexCount(),
                             1,
                             0,
                             0,
                             0);
        }
    }

    @Override
    protected void recreate() {
        this.material.tryRecreate();
    }

    @Override
    protected void cleanup() {
    }

    @Override
    public void close() {
        super.close();
        this.gpuMesh.close();
        this.material.close();
    }
}
