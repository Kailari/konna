package fi.jakojaannos.roguelite.vulkan.uniform;

import java.lang.reflect.Array;

import fi.jakojaannos.roguelite.util.BufferWriter;
import fi.jakojaannos.roguelite.vulkan.GPUBuffer;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

/**
 * A single uniform binding. Contains one or more fields.
 */
public class UniformBinding<T> implements AutoCloseable {
    private final GPUBuffer[] buffers;

    private final int sizeInBytes;
    private final BufferWriter<T> writer;

    private final int binding;
    private final int stageFlags;
    private final int descriptorCount;
    private final int descriptorType;

    public int getBinding() {
        return this.binding;
    }

    public int getStageFlags() {
        return this.stageFlags;
    }

    public int getDescriptorCount() {
        return this.descriptorCount;
    }

    private UniformBinding(
            final DeviceContext deviceContext,
            final int binding,
            final int stageFlags,
            final int descriptorCount,
            final int sizeInBytes,
            final int count,
            final BufferWriter<T> writer
    ) {
        this.descriptorType = VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;

        this.binding = binding;
        this.stageFlags = stageFlags;
        this.sizeInBytes = sizeInBytes;

        this.writer = writer;

        this.buffers = new GPUBuffer[count];
        this.descriptorCount = descriptorCount;
        for (int i = 0; i < this.buffers.length; i++) {
            this.buffers[i] = new GPUBuffer(deviceContext,
                                            sizeInBytes,
                                            VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                                            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        }
    }

    public static <T> UniformBinding<T> mutable(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final int binding,
            final int stageFlags,
            final int sizeInBytes,
            final int descriptorCount,
            final BufferWriter<T> writer
    ) {
        return new UniformBinding<>(deviceContext,
                                    binding,
                                    stageFlags,
                                    descriptorCount,
                                    sizeInBytes,
                                    swapchain.getImageCount(),
                                    writer);
    }

    // FIXME: This is very inefficient if there are multiple descriptor indices requiring updates on a single frame
    public void update(final int imageIndex, final int descriptorIndex, final T value) {
        try (final var stack = stackPush()) {
            final var data = stack.malloc(this.sizeInBytes * this.descriptorCount);
            final var offset = descriptorIndex * this.sizeInBytes;

            this.writer.write(value, offset, data);
            this.buffers[imageIndex].push(data, offset, this.sizeInBytes);
        }
    }

    @Override
    public void close() {
        for (final var buffer : this.buffers) {
            buffer.close();
        }
    }

    public GPUBuffer getBuffer(final int imageIndex) {
        return this.buffers[imageIndex];
    }

    public int getDescriptorType() {
        return this.descriptorType;
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] wrapToArray(final T value) {
        final var array = (T[]) Array.newInstance(value.getClass(), 1);
        array[0] = value;
        return array;
    }
}
