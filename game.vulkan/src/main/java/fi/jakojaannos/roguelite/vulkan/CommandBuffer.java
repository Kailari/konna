package fi.jakojaannos.roguelite.vulkan;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;

import static fi.jakojaannos.roguelite.util.VkUtil.translateVulkanResult;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class CommandBuffer implements AutoCloseable {
    private final VkCommandBuffer handle;

    public VkCommandBuffer getHandle() {
        return this.handle;
    }

    public CommandBuffer(final VkDevice device, final long handle) {
        this.handle = new VkCommandBuffer(handle, device);
    }

    public CommandBuffer begin() {
        try (final var ignored = stackPush()) {
            final var beginInfo = VkCommandBufferBeginInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            final var result = vkBeginCommandBuffer(this.handle, beginInfo);
            if (result != VK_SUCCESS) {
                throw new IllegalStateException("Starting command buffer recording failed: "
                                                + translateVulkanResult(result));
            }
        }
        return this;
    }

    public void end() {
        vkEndCommandBuffer(this.handle);
    }

    @Override
    public void close() {
        end();
    }
}
