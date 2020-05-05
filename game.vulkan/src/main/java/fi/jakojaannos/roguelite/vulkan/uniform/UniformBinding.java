package fi.jakojaannos.roguelite.vulkan.uniform;

import fi.jakojaannos.roguelite.util.BufferWriter;
import fi.jakojaannos.roguelite.util.RecreateCloseable;
import fi.jakojaannos.roguelite.vulkan.GPUBuffer;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

/**
 * A single uniform binding. Contains one or more fields.
 */
public class UniformBinding<T> extends RecreateCloseable {
    private final DeviceContext deviceContext;
    private final Swapchain swapchain;

    private final int sizeInBytes;
    private final BufferWriter<T> writer;
    private final int memoryPropertyFlags;

    private final int binding;
    private final int stageFlags;
    private final int descriptorCount;
    private final int descriptorType;

    private GPUBuffer[] buffers;
    private boolean dirty;

    public int getBinding() {
        return this.binding;
    }

    public int getStageFlags() {
        return this.stageFlags;
    }

    public int getDescriptorCount() {
        return this.descriptorCount;
    }

    public int getDescriptorType() {
        return this.descriptorType;
    }

    public boolean isDirty() {
        // Binding values must explicitly be written/updated to descriptor sets if backing memory is not HOST_COHERENT
        return this.dirty && (this.memoryPropertyFlags & VK_MEMORY_PROPERTY_HOST_COHERENT_BIT) == 0;
    }

    @Override
    protected boolean isRecreateRequired() {
        return this.buffers == null || this.buffers.length != this.swapchain.getImageCount();
    }

    public UniformBinding(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final int binding,
            final int stageFlags,
            final int descriptorCount,
            final int sizeInBytes,
            final BufferWriter<T> writer
    ) {
        this.deviceContext = deviceContext;
        this.swapchain = swapchain;
        this.descriptorType = VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
        this.descriptorCount = descriptorCount;

        this.memoryPropertyFlags = VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;

        this.binding = binding;
        this.stageFlags = stageFlags;
        this.sizeInBytes = sizeInBytes;

        this.writer = writer;
    }

    @Override
    protected void cleanup() {
        for (final var buffer : this.buffers) {
            buffer.close();
        }
    }

    @Override
    protected void recreate() {
        this.buffers = new GPUBuffer[this.swapchain.getImageCount()];
        for (int i = 0; i < this.buffers.length; i++) {
            this.buffers[i] = new GPUBuffer(this.deviceContext,
                                            this.sizeInBytes,
                                            VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                                            this.memoryPropertyFlags);
        }
    }

    // FIXME: This is very inefficient if there are multiple descriptor indices requiring updates on a single frame
    //  - overloads for "update batch" and "update full" (?)
    //  - "update batch" creates AutoCloseable scope/writer which collects a list of writes and constructs
    //    VkWriteDescriptionSets from those when batch is ended
    //  - "update full" just re-writes the whole buffer
    // The issue:
    //  - We cannot write to descriptor sets here as we do not have access to descriptor sets from bindings
    //  - Architecture here might be flawed if bindings are required to support multiple descriptor sets
    //  - one possible workaround is to create `bind`-method to the UBO and flush dirty bindings before
    //    actually binding the descriptor sets. That could make pipeline bindings messy, though, when
    //    multiple pipelines with shared UBOs are present
    public void update(final int imageIndex, final int descriptorIndex, final T value) {
        try (final var stack = stackPush()) {
            final var data = stack.malloc(this.sizeInBytes * this.descriptorCount);
            final var offset = descriptorIndex * this.sizeInBytes;

            this.writer.write(value, offset, data);
            this.buffers[imageIndex].push(data, offset, this.sizeInBytes);
        }

        this.dirty = true;
    }

    public GPUBuffer getBuffer(final int imageIndex) {
        return this.buffers[imageIndex];
    }
}
