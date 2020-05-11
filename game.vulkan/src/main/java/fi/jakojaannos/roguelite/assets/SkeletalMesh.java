package fi.jakojaannos.roguelite.assets;

import fi.jakojaannos.roguelite.vulkan.TextureSampler;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;

public class SkeletalMesh extends Mesh<SkeletalMeshVertex> {
    public SkeletalMesh(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout,
            final TextureSampler sampler,
            final SkeletalMeshVertex[] skeletalMeshVertices,
            final Integer[] indices,
            final Material material
    ) {
        super(deviceContext,
              swapchain,
              descriptorPool,
              layout,
              sampler,
              SkeletalMeshVertex.FORMAT,
              skeletalMeshVertices,
              indices,
              material);
    }
}
