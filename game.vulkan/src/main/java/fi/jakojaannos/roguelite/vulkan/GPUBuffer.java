package fi.jakojaannos.roguelite.vulkan;

import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryRequirements;

import java.nio.ByteBuffer;

import fi.jakojaannos.roguelite.vulkan.command.CommandPool;
import fi.jakojaannos.roguelite.vulkan.command.GPUQueue;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.memory.GPUMemory;

import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class GPUBuffer implements AutoCloseable {
    private final VkDevice device;

    private final GPUMemory memory;
    private final long handle;
    private final long size;

    private final boolean deviceLocal;

    public long getHandle() {
        return this.handle;
    }

    public GPUBuffer(
            final DeviceContext deviceContext,
            final long size,
            final int usageFlags,
            final int memoryPropertyFlags
    ) {
        this.device = deviceContext.getDevice();
        this.deviceLocal = (memoryPropertyFlags & VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT) != 0;

        this.size = size;
        this.handle = createBuffer(deviceContext, size, usageFlags);
        this.memory = allocateMemory(this.handle, deviceContext, memoryPropertyFlags);
        this.memory.bindBuffer(this.handle, 0);
    }

    public void push(final ByteBuffer data, final long offset, final long size) {
        if (this.deviceLocal) {
            throw new IllegalStateException("Tried directly pushing memory to a device-local buffer!");
        }

        this.memory.push(data, offset, size);
    }

    @Override
    public void close() {
        vkDestroyBuffer(this.device, this.handle, null);
        this.memory.close();
    }

    public void copyToAndWait(
            final CommandPool commandPool,
            final GPUQueue transferQueue,
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
        transferQueue.submit(commandBuffer,
                             VK_NULL_HANDLE,
                             new long[0],
                             new int[0],
                             new long[0]);

        vkQueueWaitIdle(transferQueue.getHandle());

        vkFreeCommandBuffers(this.device, commandPool.getHandle(), commandBuffer.getHandle());
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
            final int memoryPropertyFlags
    ) {
        try (final var ignored = stackPush()) {
            final var memoryRequirements = VkMemoryRequirements.callocStack();
            vkGetBufferMemoryRequirements(deviceContext.getDevice(), handle, memoryRequirements);

            return deviceContext.getMemoryManager().allocate(memoryRequirements, memoryPropertyFlags);
        }
    }
}
