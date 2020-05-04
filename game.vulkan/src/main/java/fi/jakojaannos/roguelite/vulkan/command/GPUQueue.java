package fi.jakojaannos.roguelite.vulkan.command;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo;

import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Exposes a single hardware queue for submitting commands.
 * <p>
 * Wrapper around {@link org.lwjgl.vulkan.VkQueue VkQueue}, allowing more convenient initialization and queue submit.
 */
public class GPUQueue {
    private final VkDevice device;
    private final VkQueue queue;

    /**
     * Gets the underlying raw hardware queue. Use this in cases more exotic submit tasks are required, where the
     * provided {@link #submit(CommandBuffer, long, long[], int[], long[]) submit} is not sufficient.
     *
     * @return the queue
     */
    public VkQueue getHandle() {
        return this.queue;
    }

    public GPUQueue(
            final VkDevice device,
            final int queueFamilyIndex,
            final int queueIndex
    ) {
        this.device = device;

        try (final var stack = stackPush()) {
            final var pQueue = stack.mallocPointer(1);
            vkGetDeviceQueue(this.device, queueFamilyIndex, queueIndex, pQueue);

            this.queue = new VkQueue(pQueue.get(0), this.device);
        }
    }

    public void submit(
            final CommandBuffer commandBuffer,
            final long fence,
            final long[] waitSemaphores,
            final int[] waitDstStageMask,
            final long[] signalSemaphores
    ) {
        try (final var stack = stackPush()) {
            final var submitInfo = VkSubmitInfo
                    .callocStack(1)
                    .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                    .pCommandBuffers(stack.pointers(commandBuffer.getHandle()))
                    .waitSemaphoreCount(waitSemaphores.length)
                    .pWaitSemaphores(stack.longs(waitSemaphores))
                    .pWaitDstStageMask(stack.ints(waitDstStageMask))
                    .pSignalSemaphores(stack.longs(signalSemaphores));

            if (fence != VK_NULL_HANDLE) {
                vkResetFences(this.device, fence);
            }
            ensureSuccess(vkQueueSubmit(this.queue, submitInfo, fence),
                          "Submitting command buffer to a queue failed");
        }
    }
}
