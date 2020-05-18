package fi.jakojaannos.konna.engine.assets.mesh;

import fi.jakojaannos.konna.engine.assets.material.Material;
import fi.jakojaannos.konna.engine.vulkan.TextureSampler;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.engine.vulkan.rendering.Swapchain;

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
