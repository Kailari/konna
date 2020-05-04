package fi.jakojaannos.roguelite.vulkan.memory.slice;

import java.nio.ByteBuffer;

import fi.jakojaannos.roguelite.vulkan.memory.GPUMemory;

import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memCopy;
import static org.lwjgl.vulkan.VK10.*;

public class GPUMemorySlice implements GPUMemory {
    private final SlicedGPUMemoryAllocation allocation;
    private final long offset;
    private final long size;

    public long getOffset() {
        return this.offset;
    }

    public long getSize() {
        return this.size;
    }

    public GPUMemorySlice(final SlicedGPUMemoryAllocation allocation, final long offset, final long size) {
        this.allocation = allocation;
        this.offset = offset;
        this.size = size;
    }

    public boolean overlaps(final long start, final long end) {
        return start < this.offset + this.size && end > this.offset;
    }

    @Override
    public void bindBuffer(final long handle, final long offset) {
        ensureSuccess(vkBindBufferMemory(this.allocation.getDevice(),
                                         handle,
                                         this.allocation.getHandle(),
                                         this.offset + offset),
                      "Binding buffer memory failed");
    }

    @Override
    public void bindImage(final long handle, final long offset) {
        ensureSuccess(vkBindImageMemory(this.allocation.getDevice(),
                                        handle,
                                        this.allocation.getHandle(),
                                        this.offset + offset),
                      "Binding image memory failed");
    }

    @Override
    public void push(final ByteBuffer data, final long offset, final long size) {
        final long dataAddress;
        try (final var stack = stackPush()) {
            final var pData = stack.mallocPointer(1);
            vkMapMemory(this.allocation.getDevice(),
                        this.allocation.getHandle(),
                        this.offset + offset,
                        size,
                        0,
                        pData);
            dataAddress = pData.get();
        }

        memCopy(memAddress(data), dataAddress, size);

        vkUnmapMemory(this.allocation.getDevice(), this.allocation.getHandle());
    }

    @Override
    public void close() {
        this.allocation.releaseSlice(this);
    }
}
