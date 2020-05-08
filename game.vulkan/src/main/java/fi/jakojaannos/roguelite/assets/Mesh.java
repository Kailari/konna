package fi.jakojaannos.roguelite.assets;

import fi.jakojaannos.roguelite.MaterialInstance;
import fi.jakojaannos.roguelite.util.RecreateCloseable;
import fi.jakojaannos.roguelite.vulkan.GPUBuffer;
import fi.jakojaannos.roguelite.vulkan.GPUMesh;
import fi.jakojaannos.roguelite.vulkan.TextureSampler;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;

public class Mesh extends RecreateCloseable {
    private final GPUMesh<MeshVertex> gpuMesh;
    private final MaterialInstance material;

    public GPUBuffer getVertexBuffer() {
        return this.gpuMesh.getVertexBuffer();
    }

    public GPUBuffer getIndexBuffer() {
        return this.gpuMesh.getIndexBuffer();
    }

    public int getIndexCount() {
        return this.gpuMesh.getIndexCount();
    }

    @Override
    protected boolean isRecreateRequired() {
        return this.material.isRecreateRequired();
    }

    public MaterialInstance getMaterialInstance() {
        return this.material;
    }

    public Mesh(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout,
            final TextureSampler sampler,
            final MeshVertex[] vertices,
            final Short[] indices,
            final Material material
    ) {
        this.material = new MaterialInstance(deviceContext,
                                             swapchain,
                                             descriptorPool,
                                             layout,
                                             sampler,
                                             material);
        this.gpuMesh = new GPUMesh<>(deviceContext,
                                     MeshVertex.FORMAT,
                                     vertices,
                                     indices);
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
