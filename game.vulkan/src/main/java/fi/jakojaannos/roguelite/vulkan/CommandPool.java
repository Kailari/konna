package fi.jakojaannos.roguelite.vulkan;

import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;

import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;

import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class CommandPool implements AutoCloseable {
    private final VkDevice device;
    private final long handle;

    public CommandPool(final DeviceContext deviceContext, final int queueFamilyIndex) {
        this.device = deviceContext.getDevice();

        try (final var stack = stackPush()) {
            final var createInfo = VkCommandPoolCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                    .queueFamilyIndex(queueFamilyIndex)
                    .flags(0);

            final var pPool = stack.mallocLong(1);
            ensureSuccess(vkCreateCommandPool(this.device, createInfo, null, pPool),
                          "Creating command pool failed");
            this.handle = pPool.get(0);
        }
    }

    public CommandBuffer[] allocate(final int count) {
        try (final var stack = stackPush()) {
            final var allocInfo = VkCommandBufferAllocateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                    .commandPool(this.handle)
                    .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(count);

            final var pBuffers = stack.mallocPointer(count);
            ensureSuccess(vkAllocateCommandBuffers(this.device, allocInfo, pBuffers),
                          "Allocating command buffers failed");

            final var buffers = new CommandBuffer[count];
            for (int i = 0; i < count; i++) {
                buffers[i] = new CommandBuffer(this.device, pBuffers.get(i));
            }
            return buffers;
        }
    }

    @Override
    public void close() {
        vkDestroyCommandPool(this.device, this.handle, null);
    }
}
