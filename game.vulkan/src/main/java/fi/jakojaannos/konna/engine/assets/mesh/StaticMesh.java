package fi.jakojaannos.konna.engine.assets.mesh;

import fi.jakojaannos.konna.engine.assets.material.Material;
import fi.jakojaannos.konna.engine.vulkan.TextureSampler;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.engine.vulkan.rendering.Swapchain;

public class StaticMesh extends Mesh<StaticMeshVertex> {
    public StaticMesh(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout,
            final TextureSampler sampler,
            final StaticMeshVertex[] staticMeshVertices,
            final Integer[] indices,
            final Material material
    ) {
        super(deviceContext,
              swapchain,
              descriptorPool,
              layout,
              sampler,
              StaticMeshVertex.FORMAT,
              staticMeshVertices,
              indices,
              material);
    }
}
