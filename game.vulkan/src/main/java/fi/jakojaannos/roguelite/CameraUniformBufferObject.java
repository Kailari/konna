package fi.jakojaannos.roguelite;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.nio.ByteBuffer;

import fi.jakojaannos.roguelite.util.RecreateCloseable;
import fi.jakojaannos.roguelite.vulkan.GPUBuffer;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorBinding;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;
import fi.jakojaannos.roguelite.vulkan.types.VkMemoryPropertyFlags;

import static fi.jakojaannos.roguelite.util.BitMask.bitMask;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class CameraUniformBufferObject extends RecreateCloseable {
    private final DeviceContext deviceContext;
    private final Swapchain swapchain;
    private final DescriptorPool descriptorPool;
    private final DescriptorSetLayout layout;

    private final long minUniformBufferOffsetAlignment;

    private final CameraMatrices cameraMatrices;
    private final InstanceMatrices instanceMatrices;
    private final UniformBinding[] bindings;

    private final Vector3f eyePosition;
    private final Vector3f lookAtTarget;
    private final Vector3f up;

    private GPUBuffer[] buffers;
    private long[] bindingOffsets;
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

        this.cameraMatrices = new CameraMatrices();
        this.instanceMatrices = new InstanceMatrices();
        this.bindings = new UniformBinding[]{this.cameraMatrices, this.instanceMatrices};
        this.layout = new DescriptorSetLayout(deviceContext,
                                              this.cameraMatrices.getDescriptorBinding(),
                                              this.instanceMatrices.getDescriptorBinding());

        this.eyePosition = new Vector3f(0.0f, -3.0f, 2.0f);
        this.lookAtTarget = new Vector3f(0.0f, 0.0f, 0.0f);
        this.up = new Vector3f(0.0f, 0.0f, 1.0f);

        this.minUniformBufferOffsetAlignment = deviceContext.getDeviceProperties()
                                                            .limits()
                                                            .minUniformBufferOffsetAlignment();
    }

    public long getDescriptorSet(final int imageIndex) {
        return this.descriptorSets[imageIndex];
    }

    @Override
    protected void recreate() {
        this.buffers = new GPUBuffer[this.swapchain.getImageCount()];
        this.bindingOffsets = new long[this.bindings.length];

        final var minAlign = this.minUniformBufferOffsetAlignment;
        for (int imageIndex = 0; imageIndex < this.buffers.length; imageIndex++) {
            var accumulatedSize = 0;

            // Calculate offsets for the bindings within the shared buffer
            for (int bindingIndex = 0; bindingIndex < this.bindings.length; bindingIndex++) {
                // Size is determined by the minimum required alignment of a single binding (which
                // may be hardware-specific). We must start every new binding from offset which is
                // a multiple of the minimum alignment. Yes, this potentially ends up wasting loads
                // of memory, but as memory allocations have similar alignment requirements, this
                // should be at least a bit more efficient when there are many descriptors within
                // a single UBO.
                final var size = this.bindings[bindingIndex].getSizeInBytes();
                final var n = (int) Math.ceil(size / (double) minAlign);
                accumulatedSize += n * minAlign;

                // Update offset of the next binding, unless we are at the last binding already.
                // The first binding always has offset of zero.
                if (bindingIndex + 1 < this.bindings.length) {
                    this.bindingOffsets[bindingIndex + 1] = accumulatedSize;
                }
            }

            this.buffers[imageIndex] = new GPUBuffer(this.deviceContext,
                                                     accumulatedSize,
                                                     VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                                                     bitMask(VkMemoryPropertyFlags.HOST_VISIBLE_BIT,
                                                             VkMemoryPropertyFlags.HOST_COHERENT_BIT));
        }

        this.descriptorSets = this.descriptorPool.allocate(this.layout, this.swapchain.getImageCount());

        for (int imageIndex = 0; imageIndex < this.descriptorSets.length; ++imageIndex) {
            try (final var ignored = stackPush()) {
                final var descriptorWrites = VkWriteDescriptorSet.callocStack(2);

                final var buffer = this.buffers[imageIndex];
                for (int bindingIndex = 0; bindingIndex < this.bindings.length; ++bindingIndex) {
                    final var binding = this.bindings[bindingIndex];
                    final var descriptor = binding.getDescriptorBinding();
                    descriptorWrites.get(bindingIndex)
                                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                                    .dstSet(this.descriptorSets[imageIndex])
                                    .dstBinding(descriptor.slot())
                                    .descriptorType(descriptor.descriptorType())
                                    .descriptorCount(descriptor.descriptorCount())
                                    .pBufferInfo(VkDescriptorBufferInfo.callocStack(1)
                                                                       .buffer(buffer.getHandle())
                                                                       .offset(this.bindingOffsets[bindingIndex])
                                                                       .range(binding.getSizeInBytes()))
                                    .dstArrayElement(0);
                }

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
        for (final var buffer : this.buffers) {
            buffer.close();
        }
    }

    @Override
    public void close() {
        super.close();
        this.layout.close();
    }

    public void update(final int imageIndex, final double angle) {
        final var aspectRatio = this.swapchain.getExtent().width() / (float) this.swapchain.getExtent().height();
        // Create right-handed perspective projection matrix with Y-axis flipped
        // (Vulkan NDC has Y pointing down which we need to correct with projection matrix)
        //
        // This results in coordinate system where:
        //  -X: Left       +X: Right
        //  -Y: Back       +Y: Forward
        //  -Z: Down       +Z: Up
        this.cameraMatrices.projection.identity()
                                      .scale(1, -1, 1)
                                      .perspective((float) Math.toRadians(45.0),
                                                   aspectRatio,
                                                   0.1f, 1000.0f, true);

        this.cameraMatrices.view.identity().lookAt(this.eyePosition, this.lookAtTarget, this.up);

        this.instanceMatrices.model.identity().rotateZ((float) angle);

        try (final var stack = stackPush()) {
            final var data = stack.malloc((int) this.buffers[imageIndex].getSize());

            for (int bindingIndex = 0; bindingIndex < this.bindings.length; bindingIndex++) {
                this.bindings[bindingIndex].write((int) this.bindingOffsets[bindingIndex], data);
            }
            this.buffers[imageIndex].push(data, 0, this.buffers[imageIndex].getSize());
        }
    }

    private interface UniformBinding {
        long getSizeInBytes();

        DescriptorBinding getDescriptorBinding();

        void write(int offset, ByteBuffer buffer);
    }

    private static class CameraMatrices implements UniformBinding {
        private static final int SIZE_IN_BYTES = 2 * 16 * Float.BYTES;

        private static final int OFFSET_VIEW = 0;
        private static final int OFFSET_PROJECTION = 16 * Float.BYTES;

        private final DescriptorBinding descriptorBinding;
        private final Matrix4f view = new Matrix4f().identity();
        private final Matrix4f projection = new Matrix4f().identity();

        @Override
        public DescriptorBinding getDescriptorBinding() {
            return this.descriptorBinding;
        }

        @Override
        public long getSizeInBytes() {
            return SIZE_IN_BYTES;
        }

        private CameraMatrices() {
            this.descriptorBinding = new DescriptorBinding(0,
                                                           VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                           1,
                                                           VK_SHADER_STAGE_VERTEX_BIT);
        }

        @Override
        public void write(final int offset, final ByteBuffer buffer) {
            this.view.get(offset + OFFSET_VIEW, buffer);
            this.projection.get(offset + OFFSET_PROJECTION, buffer);
        }
    }

    private static class InstanceMatrices implements UniformBinding {
        private static final int SIZE_IN_BYTES = 16 * Float.BYTES;

        private static final int OFFSET_MODEL = 0;
        private final DescriptorBinding descriptorBinding;

        private final Matrix4f model = new Matrix4f().identity();

        @Override
        public DescriptorBinding getDescriptorBinding() {
            return this.descriptorBinding;
        }

        @Override
        public long getSizeInBytes() {
            return SIZE_IN_BYTES;
        }

        private InstanceMatrices() {
            this.descriptorBinding = new DescriptorBinding(1,
                                                           VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                           1,
                                                           VK_SHADER_STAGE_VERTEX_BIT);
        }

        @Override
        public void write(final int offset, final ByteBuffer buffer) {
            this.model.get(offset + OFFSET_MODEL, buffer);
        }
    }
}
