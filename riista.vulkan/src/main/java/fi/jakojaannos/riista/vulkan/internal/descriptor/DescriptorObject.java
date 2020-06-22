package fi.jakojaannos.riista.vulkan.internal.descriptor;

import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.util.function.IntSupplier;

import fi.jakojaannos.riista.vulkan.internal.GPUBuffer;
import fi.jakojaannos.riista.vulkan.internal.device.DeviceContext;
import fi.jakojaannos.riista.vulkan.internal.types.VkDescriptorType;
import fi.jakojaannos.riista.vulkan.internal.types.VkMemoryPropertyFlags;
import fi.jakojaannos.riista.vulkan.util.RecreateCloseable;

import static fi.jakojaannos.riista.utilities.BitMask.bitMask;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Collection of descriptor sets. May contain uniform buffers and/or samplers.
 */
public abstract class DescriptorObject extends RecreateCloseable {
    private final DeviceContext deviceContext;
    private final IntSupplier descriptorCountSupplier;
    private final DescriptorPool descriptorPool;

    private final DescriptorSetLayout layout;
    private final CombinedImageSamplerBinding[] imageSamplerBindings;
    private final UniformBufferBinding[] uniformBindings;

    private final long[] bindingOffsets;
    private final long bufferSizeInBytes;

    private GPUBuffer[] buffers;
    private long[] descriptorSets;

    public DescriptorSetLayout getLayout() {
        return this.layout;
    }

    @Override
    protected boolean isRecreateRequired() {
        return isOlderThan(this.descriptorPool);
    }

    public DescriptorObject(
            final DeviceContext deviceContext,
            final IntSupplier descriptorCountSupplier,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout,
            final CombinedImageSamplerBinding[] imageSamplerBindings,
            final UniformBufferBinding[] uniformBindings
    ) {
        this.deviceContext = deviceContext;
        this.descriptorCountSupplier = descriptorCountSupplier;
        this.descriptorPool = descriptorPool;
        this.layout = layout;

        this.imageSamplerBindings = imageSamplerBindings;
        this.uniformBindings = uniformBindings;

        this.bindingOffsets = new long[this.uniformBindings.length];

        // Size is determined by the minimum required alignment of a single binding (which is
        // hardware/driver-specific). We must start every new binding from offset which is a
        // multiple of the minimum alignment. Yes, this potentially ends up wasting loads of memory,
        // but as memory allocations have similar alignment requirements, this should be at least
        // a bit more efficient when there are many descriptors within a single UBO.
        final long minAlign = deviceContext.getDeviceProperties()
                                           .limits()
                                           .minUniformBufferOffsetAlignment();

        // Calculate offsets for the bindings within the shared buffer
        var accumulatedSize = 0L;
        for (int bindingIndex = 0; bindingIndex < this.uniformBindings.length; bindingIndex++) {
            final var size = this.uniformBindings[bindingIndex].sizeInBytes();
            final var n = (int) Math.ceil(size / (double) minAlign);
            accumulatedSize += n * minAlign;

            // Update offset of the next binding, unless we are at the last binding already.
            // The first binding always has offset of zero.
            if (bindingIndex + 1 < this.uniformBindings.length) {
                this.bindingOffsets[bindingIndex + 1] = accumulatedSize;
            }
        }
        this.bufferSizeInBytes = accumulatedSize;
    }

    public long getDescriptorSet(final int index) {
        return this.descriptorSets[index];
    }

    protected void flushAllUniformBufferBindings(final int index) {
        try (final var stack = stackPush()) {
            final var buffer = this.buffers[index];
            final var data = stack.malloc((int) buffer.getSize());

            for (int i = 0; i < this.uniformBindings.length; i++) {
                this.uniformBindings[i].write((int) this.bindingOffsets[i], data);
            }

            buffer.push(data, 0, buffer.getSize());
        }
    }

    protected void flushAllCombinedImageSamplerBindings(final int index) {
        try (final var ignored = stackPush()) {
            final var writes = VkWriteDescriptorSet
                    .callocStack(this.imageSamplerBindings.length);

            var writeIndex = 0;
            for (final var imageSamplerBinding : this.imageSamplerBindings) {
                writeCombinedImageSampler(imageSamplerBinding,
                                          this.descriptorSets[index],
                                          writes.get(writeIndex));
                ++writeIndex;
            }

            vkUpdateDescriptorSets(this.deviceContext.getDevice(), writes, null);
        }
    }

    @Override
    protected void recreate() {
        final var descriptorCount = this.descriptorCountSupplier.getAsInt();
        if (this.bufferSizeInBytes > 0) {
            this.buffers = new GPUBuffer[descriptorCount];
        } else {
            this.buffers = new GPUBuffer[0];
        }

        for (int imageIndex = 0; imageIndex < this.buffers.length; imageIndex++) {
            this.buffers[imageIndex] = new GPUBuffer(this.deviceContext,
                                                     this.bufferSizeInBytes,
                                                     VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                                                     bitMask(VkMemoryPropertyFlags.HOST_VISIBLE_BIT,
                                                             VkMemoryPropertyFlags.HOST_COHERENT_BIT));
            flushAllUniformBufferBindings(imageIndex);
        }

        this.descriptorSets = this.descriptorPool.allocate(this.layout, descriptorCount);

        try (final var ignored = stackPush()) {
            final var bindingCount = this.uniformBindings.length + this.imageSamplerBindings.length;
            final var writes = VkWriteDescriptorSet.callocStack(bindingCount * descriptorCount);

            var writeIndex = 0;
            for (int imageIndex = 0; imageIndex < descriptorCount; ++imageIndex) {
                for (int bindingIndex = 0; bindingIndex < this.uniformBindings.length; ++bindingIndex) {
                    writeBuffer(this.uniformBindings[bindingIndex],
                                this.buffers[imageIndex],
                                this.bindingOffsets[bindingIndex],
                                this.descriptorSets[imageIndex],
                                writes.get(writeIndex));
                    ++writeIndex;
                }

                for (final var imageSamplerBinding : this.imageSamplerBindings) {
                    writeCombinedImageSampler(imageSamplerBinding,
                                              this.descriptorSets[imageIndex],
                                              writes.get(writeIndex));
                    ++writeIndex;
                }
            }

            vkUpdateDescriptorSets(this.deviceContext.getDevice(), writes, null);
        }
    }

    @Override
    protected void cleanup() {
        for (final var buffer : this.buffers) {
            buffer.close();
        }

        // Release descriptors if descriptor pool has not been reset. This means that a subclass
        // has defined a re-create condition which does not require re-creating the whole pool.
        // Additionally, make sure the pool is not cleaned up (to prevent crash when shutting down)
        if (!isOlderThan(this.descriptorPool) && !this.descriptorPool.isCleanedUp()) {
            this.descriptorPool.free(this.descriptorSets);
        }
    }

    private void writeBuffer(
            final UniformBufferBinding binding,
            final GPUBuffer buffer,
            final long offset,
            final long descriptorSet,
            final VkWriteDescriptorSet write
    ) {
        write.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
             .descriptorCount(1)
             .descriptorType(VkDescriptorType.UNIFORM_BUFFER.asInt())
             .dstSet(descriptorSet)
             .dstBinding(binding.binding())
             .dstArrayElement(0)
             .pBufferInfo(VkDescriptorBufferInfo.callocStack(1)
                                                .buffer(buffer.getHandle())
                                                .offset(offset)
                                                .range(binding.sizeInBytes()));
    }

    private void writeCombinedImageSampler(
            final CombinedImageSamplerBinding binding,
            final long descriptorSet,
            final VkWriteDescriptorSet write
    ) {
        write.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
             .descriptorCount(1)
             .descriptorType(VkDescriptorType.COMBINED_IMAGE_SAMPLER.asInt())
             .dstSet(descriptorSet)
             .dstBinding(binding.binding())
             .dstArrayElement(0)
             .pImageInfo(VkDescriptorImageInfo.callocStack(1)
                                              .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                                              .imageView(binding.imageView().getHandle())
                                              .sampler(binding.sampler().getHandle()));
    }
}
