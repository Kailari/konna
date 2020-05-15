package fi.jakojaannos.konna.vulkan.rendering;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import static fi.jakojaannos.konna.util.VkUtil.translateVulkanResult;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class Framebuffer implements AutoCloseable {
    private final VkDevice device;
    private final VkExtent2D swapchainExtent;

    private final long handle;

    public VkExtent2D getExtent() {
        return this.swapchainExtent;
    }

    public long getHandle() {
        return this.handle;
    }

    public Framebuffer(
            final VkDevice device,
            final VkExtent2D swapchainExtent,
            final ImageView swapchainImageView,
            final ImageView depthImageView,
            final RenderPass renderPass
    ) {
        this.device = device;
        this.swapchainExtent = swapchainExtent;

        try (final var stack = stackPush()) {
            final var createInfo = VkFramebufferCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                    .renderPass(renderPass.getHandle())
                    .pAttachments(stack.longs(swapchainImageView.getHandle(),
                                              depthImageView.getHandle()))
                    .width(swapchainExtent.width())
                    .height(swapchainExtent.height())
                    .layers(1);

            final var pFramebuffer = stack.mallocLong(1);
            final var result = vkCreateFramebuffer(this.device, createInfo, null, pFramebuffer);
            if (result != VK_SUCCESS) {
                throw new IllegalStateException("Creating framebuffer failed: "
                                                + translateVulkanResult(result));
            }
            this.handle = pFramebuffer.get(0);
        }
    }

    @Override
    public void close() {
        vkDestroyFramebuffer(this.device, this.handle, null);
    }
}
