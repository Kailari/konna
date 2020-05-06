package fi.jakojaannos.roguelite;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.nio.ByteBuffer;

import fi.jakojaannos.roguelite.util.RecreateCloseable;
import fi.jakojaannos.roguelite.vulkan.GPUBuffer;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorSetBinding;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.roguelite.vulkan.descriptor.uniform.DescriptorPool;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class CameraUniformBufferObject extends RecreateCloseable {
    private final DeviceContext deviceContext;
    private final Swapchain swapchain;
    private final DescriptorPool descriptorPool;
    private final DescriptorSetLayout layout;

    private final CameraMatrices cameraMatrices;
    private final InstanceMatrices instanceMatrices;

    private final Vector3f eyePosition;
    private final Vector3f lookAtTarget;
    private final Vector3f up;

    private final GPUBuffer[][] buffers;
    private long[] descriptorSets;

    @Override
    protected boolean isRecreateRequired() {
        return true;
    }

    public DescriptorSetLayout getLayout() {
        return this.layout;
    }

    public CameraUniformBufferObject(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool
    ) {
        this.deviceContext = deviceContext;
        this.swapchain = swapchain;
        this.descriptorPool = descriptorPool;

        this.layout = new DescriptorSetLayout(deviceContext,
                                              new DescriptorSetBinding(0,
                                                                       VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                                       1,
                                                                       VK_SHADER_STAGE_VERTEX_BIT),
                                              new DescriptorSetBinding(1,
                                                                       VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                                       1,
                                                                       VK_SHADER_STAGE_VERTEX_BIT));
        this.buffers = new GPUBuffer[2][];

        this.cameraMatrices = new CameraMatrices();
        this.instanceMatrices = new InstanceMatrices();

        this.eyePosition = new Vector3f(2.0f, 2.0f, 2.0f);
        this.lookAtTarget = new Vector3f(0.0f, 0.0f, 0.0f);
        this.up = new Vector3f(0.0f, 0.0f, 1.0f);

        tryRecreate();
    }

    public long getDescriptorSet(final int imageIndex) {
        return this.descriptorSets[imageIndex];
    }

    @Override
    protected void recreate() {
        this.buffers[0] = new GPUBuffer[this.swapchain.getImageCount()];
        this.buffers[1] = new GPUBuffer[this.swapchain.getImageCount()];
        for (int i = 0; i < this.swapchain.getImageCount(); i++) {
            this.buffers[0][i] = new GPUBuffer(this.deviceContext,
                                               CameraMatrices.SIZE_IN_BYTES,
                                               VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                                               VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
            this.buffers[1][i] = new GPUBuffer(this.deviceContext,
                                               InstanceMatrices.SIZE_IN_BYTES,
                                               VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                                               VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        }

        this.descriptorSets = this.descriptorPool.allocate(this.layout, this.swapchain.getImageCount());

        try (final var ignored = stackPush()) {
            for (int imageIndex = 0; imageIndex < this.descriptorSets.length; imageIndex++) {
                final var descriptorWrites = VkWriteDescriptorSet.callocStack(2);

                // FIXME: This is basically "full write for all bindings"? Bindings should be generalized somehow to allow iterating over them
                final var matrixBuffer = this.buffers[0][imageIndex];
                descriptorWrites.get(0)
                                .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                                .dstSet(this.descriptorSets[imageIndex])
                                .dstBinding(0)
                                .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                                .descriptorCount(1)
                                .pBufferInfo(VkDescriptorBufferInfo.callocStack(1)
                                                                   .buffer(matrixBuffer.getHandle())
                                                                   .offset(0)
                                                                   .range(CameraMatrices.SIZE_IN_BYTES))
                                .dstArrayElement(0);

                final var instanceBuffer = this.buffers[1][imageIndex];
                descriptorWrites.get(1)
                                .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                                .dstSet(this.descriptorSets[imageIndex])
                                .dstBinding(1)
                                .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                                .descriptorCount(1)
                                .pBufferInfo(VkDescriptorBufferInfo.callocStack(1)
                                                                   .buffer(instanceBuffer.getHandle())
                                                                   .offset(0)
                                                                   .range(InstanceMatrices.SIZE_IN_BYTES))
                                .dstArrayElement(0);

                // NOTE: Why don't we need to call this every frame? Well, the bindings allocate the buffers
                //       as HOST_COHERENT, so we point the GPU at the memory once and it knows where to look
                //       from there on.
                // FIXME: This should happen in update(), too (in cases where backing buffer is not HOST_COHERENT)
                vkUpdateDescriptorSets(this.deviceContext.getDevice(), descriptorWrites, null);
            }
        }
    }

    @Override
    protected void cleanup() {
        for (final var bindingBuffers : this.buffers) {
            for (final var buffer : bindingBuffers) {
                buffer.close();
            }
        }
    }

    @Override
    public void close() {
        super.close();
        this.layout.close();
    }

    public void update(final int imageIndex, final double angle) {
        final var aspectRatio = this.swapchain.getExtent().width() / (float) this.swapchain.getExtent().height();
        this.cameraMatrices.projection.identity()
                                      .perspective((float) Math.toRadians(45.0),
                                                   aspectRatio,
                                                   0.1f, 1000.0f, true);
        // Flip the Y-axis
        this.cameraMatrices.projection.m11(this.cameraMatrices.projection.m11() * -1.0f);

        this.cameraMatrices.view.identity().lookAt(this.eyePosition, this.lookAtTarget, this.up);

        this.instanceMatrices.model.identity().rotateZ((float) angle);

        try (final var stack = stackPush()) {
            final var data = stack.malloc(CameraMatrices.SIZE_IN_BYTES);

            this.cameraMatrices.write(0, data);
            this.buffers[0][imageIndex].push(data, 0, CameraMatrices.SIZE_IN_BYTES);
        }
        try (final var stack = stackPush()) {
            final var data = stack.malloc(InstanceMatrices.SIZE_IN_BYTES);

            this.instanceMatrices.write(0, data);
            this.buffers[1][imageIndex].push(data, 0, InstanceMatrices.SIZE_IN_BYTES);
        }
    }

    private static class InstanceMatrices {
        private static final int SIZE_IN_BYTES = 16 * Float.BYTES;

        private static final int OFFSET_MODEL = 0;

        private final Matrix4f model = new Matrix4f().identity();

        public void write(final int offset, final ByteBuffer buffer) {
            this.model.get(offset + OFFSET_MODEL, buffer);
        }
    }

    private static class CameraMatrices {
        private static final int SIZE_IN_BYTES = 2 * 16 * Float.BYTES;

        private static final int OFFSET_VIEW = 0;
        private static final int OFFSET_PROJECTION = 16 * Float.BYTES;

        private final Matrix4f view = new Matrix4f().identity();
        private final Matrix4f projection = new Matrix4f().identity();

        public void write(final int offset, final ByteBuffer buffer) {
            this.view.get(offset + OFFSET_VIEW, buffer);
            this.projection.get(offset + OFFSET_PROJECTION, buffer);
        }
    }
}
