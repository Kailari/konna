package fi.jakojaannos.roguelite.assets;

import fi.jakojaannos.roguelite.vulkan.TextureSampler;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;

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
