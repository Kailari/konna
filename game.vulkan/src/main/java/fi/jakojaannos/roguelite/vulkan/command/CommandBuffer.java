package fi.jakojaannos.roguelite.vulkan.command;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;

import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
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
        return begin(0);
    }

    public CommandBuffer begin(final int flags) {
        try (final var ignored = stackPush()) {
            final var beginInfo = VkCommandBufferBeginInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                    .flags(flags);
            ensureSuccess(vkBeginCommandBuffer(this.handle, beginInfo),
                          "Starting command buffer recording failed");
        }
        return this;
    }

    public void end() {
        ensureSuccess(vkEndCommandBuffer(this.handle),
                      "Finalizing command buffer recording failed");
    }

    @Override
    public void close() {
        end();
    }
}
