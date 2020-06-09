package fi.jakojaannos.konna.engine.vulkan;

import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;

import fi.jakojaannos.riista.utilities.BitMask;
import fi.jakojaannos.konna.engine.util.BufferWriter;
import fi.jakojaannos.konna.engine.vulkan.command.CommandPool;
import fi.jakojaannos.konna.engine.vulkan.command.GPUQueue;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.engine.vulkan.memory.GPUMemory;
import fi.jakojaannos.konna.engine.vulkan.types.VkMemoryPropertyFlags;

import static fi.jakojaannos.riista.utilities.BitMask.bitMask;
import static fi.jakojaannos.konna.engine.util.VkUtil.ensureSuccess;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.*;

public class GPUBuffer implements AutoCloseable {
    private final DeviceContext deviceContext;

    private final GPUMemory memory;
    private final long handle;
    private final long size;

    private final boolean deviceLocal;

    public long getHandle() {
        return this.handle;
    }

    public GPUMemory getMemory() {
        return this.memory;
    }

    public long getSize() {
        return this.size;
    }

    public GPUBuffer(
            final DeviceContext deviceContext,
            final long size,
            final int usageFlags,
            final BitMask<VkMemoryPropertyFlags> memoryProperties
    ) {
        this.deviceContext = deviceContext;
        this.deviceLocal = memoryProperties.hasBit(VkMemoryPropertyFlags.DEVICE_LOCAL_BIT);

        this.size = size;
        this.handle = createBuffer(deviceContext, size, usageFlags);
        this.memory = allocateMemory(this.handle, deviceContext, memoryProperties);
        this.memory.bindBuffer(this.handle, 0);
    }

    public <T> void pushWithStagingAndWait(
            final T[] values,
            final int elementSize,
            final BufferWriter<T> writer
    ) {
        final var commandPool = this.deviceContext.getTransferCommandPool();
        final var dataSizeInBytes = values.length * elementSize;

        final var data = memAlloc(dataSizeInBytes);
        try (final var stagingBuffer = new GPUBuffer(
                this.deviceContext,
                dataSizeInBytes,
                VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                bitMask(VkMemoryPropertyFlags.HOST_VISIBLE_BIT,
                        VkMemoryPropertyFlags.HOST_COHERENT_BIT))
        ) {
            for (int i = 0; i < values.length; ++i) {
                writer.write(values[i], i * elementSize, data);
            }

            stagingBuffer.push(data, 0, dataSizeInBytes);
            stagingBuffer.copyToAndWait(commandPool, this.deviceContext.getTransferQueue(), this);
        } finally {
            memFree(data);
        }
    }

    public void push(final ByteBuffer data, final long offset, final long size) {
        if (this.deviceLocal) {
            throw new IllegalStateException("Tried directly pushing memory to a device-local buffer!");
        }

        this.memory.push(data, offset, size);
    }

    @Override
    public void close() {
        vkDestroyBuffer(this.deviceContext.getDevice(), this.handle, null);
        this.memory.close();
    }

    public void copyToAndWait(
            final CommandPool commandPool,
            final GPUQueue queue,
            final GPUBuffer target
    ) {
        final var commandBuffer = commandPool.allocate(1)[0];
        try (final var ignored = stackPush();
             final var ignored2 = commandBuffer.begin(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
        ) {
            // Execute the copy
            final var copyRegions = VkBufferCopy
                    .callocStack(1)
                    .srcOffset(0)
                    .dstOffset(0)
                    .size(this.size);

            vkCmdCopyBuffer(commandBuffer.getHandle(),
                            this.handle,
                            target.handle,
                            copyRegions);
        }
        queue.submit(commandBuffer,
                             VK_NULL_HANDLE,
                             new long[0],
                             new int[0],
                             new long[0]);

        vkQueueWaitIdle(queue.getHandle());

        vkFreeCommandBuffers(this.deviceContext.getDevice(), commandPool.getHandle(), commandBuffer.getHandle());
    }

    public void copyToAndWait(
            final CommandPool commandPool,
            final GPUQueue queue,
            final GPUImage target
    ) {
        final var commandBuffer = commandPool.allocate(1)[0];
        try (final var ignored = stackPush();
             final var ignored2 = commandBuffer.begin(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
        ) {
            // Execute the copy
            final var copyRegion = VkBufferImageCopy
                    .callocStack(1)
                    .bufferOffset(0)
                    .bufferRowLength(0)
                    .bufferImageHeight(0);
            copyRegion.imageSubresource()
                      .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                      .mipLevel(0)
                      .baseArrayLayer(0)
                      .layerCount(1);
            copyRegion.imageOffset()
                      .set(0, 0, 0);
            copyRegion.imageExtent()
                      .set(target.getWidth(), target.getHeight(), 1);

            vkCmdCopyBufferToImage(commandBuffer.getHandle(),
                                   this.handle,
                                   target.getHandle(),
                                   VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                                   copyRegion);
        }
        queue.submit(commandBuffer,
                             VK_NULL_HANDLE,
                             new long[0],
                             new int[0],
                             new long[0]);

        vkQueueWaitIdle(queue.getHandle());

        vkFreeCommandBuffers(this.deviceContext.getDevice(), commandPool.getHandle(), commandBuffer.getHandle());
    }

    private static long createBuffer(
            final DeviceContext deviceContext,
            final long size,
            final int usageFlags
    ) {
        try (final var stack = stackPush()) {
            final var graphicsFamilyIndex = deviceContext.getQueueFamilies().graphics();
            final var transferFamilyIndex = deviceContext.getQueueFamilies().transfer();
            final var pQueueFamilyIndices = graphicsFamilyIndex != transferFamilyIndex
                    ? stack.ints(graphicsFamilyIndex, transferFamilyIndex)
                    : stack.ints(graphicsFamilyIndex);

            // FIXME: This should not be necessary; transfer ownership to one queue at a time using
            //        barriers or sth
            final var sharingMode = graphicsFamilyIndex != transferFamilyIndex
                    ? VK_SHARING_MODE_CONCURRENT
                    : VK_SHARING_MODE_EXCLUSIVE;

            final var bufferInfo = VkBufferCreateInfo
                    .callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                    .size(size)
                    .usage(usageFlags)
                    .sharingMode(sharingMode)
                    .pQueueFamilyIndices(pQueueFamilyIndices);

            final var pBuffer = stack.mallocLong(1);
            ensureSuccess(vkCreateBuffer(deviceContext.getDevice(),
                                         bufferInfo,
                                         null,
                                         pBuffer),
                          "Creating GPU buffer failed");

            return pBuffer.get(0);
        }
    }

    private static GPUMemory allocateMemory(
            final long handle,
            final DeviceContext deviceContext,
            final BitMask<VkMemoryPropertyFlags> memoryProperties
    ) {
        try (final var ignored = stackPush()) {
            final var memoryRequirements = VkMemoryRequirements.callocStack();
            vkGetBufferMemoryRequirements(deviceContext.getDevice(), handle, memoryRequirements);

            return deviceContext.getMemoryManager().allocate(memoryRequirements, memoryProperties);
        }
    }
}
