package fi.jakojaannos.konna.engine.assets.mesh;

import fi.jakojaannos.konna.engine.assets.material.MaterialInstance;
import fi.jakojaannos.konna.engine.assets.material.Material;
import fi.jakojaannos.konna.engine.util.RecreateCloseable;
import fi.jakojaannos.konna.engine.vulkan.GPUMesh;
import fi.jakojaannos.konna.engine.vulkan.TextureSampler;
import fi.jakojaannos.konna.engine.vulkan.VertexFormat;
import fi.jakojaannos.konna.engine.vulkan.command.CommandBuffer;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.engine.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.konna.engine.vulkan.rendering.Swapchain;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class Mesh<TVertex> extends RecreateCloseable {
    private final GPUMesh<TVertex> gpuMesh;
    private final MaterialInstance material;

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
            this.gpuMesh.draw(commandBuffer);
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
