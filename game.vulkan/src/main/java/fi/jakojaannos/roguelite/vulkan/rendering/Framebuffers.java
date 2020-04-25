package fi.jakojaannos.roguelite.vulkan.rendering;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;

import static fi.jakojaannos.roguelite.util.VkUtil.translateVulkanResult;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class Framebuffers implements AutoCloseable {
    private final VkDevice device;
    private final long[] framebuffers;

    public Framebuffers(
            final DeviceContext deviceContext,
            final VkExtent2D swapchainExtent,
            final ImageView[] swapchainImageViews,
            final RenderPass renderPass
    ) {
        this.device = deviceContext.getDevice();
        this.framebuffers = new long[swapchainImageViews.length];

        for (int i = 0; i < this.framebuffers.length; i++) {
            this.framebuffers[i] = createFramebuffer(swapchainExtent,
                                                     swapchainImageViews[i],
                                                     renderPass);
        }
    }

    private long createFramebuffer(
            final VkExtent2D swapchainExtent,
            final ImageView swapchainImageView,
            final RenderPass renderPass
    ) {
        try (final var stack = stackPush()) {
            final var createInfo = VkFramebufferCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                    .renderPass(renderPass.getHandle())
                    .pAttachments(stack.longs(swapchainImageView.getHandle()))
                    .width(swapchainExtent.width())
                    .height(swapchainExtent.height())
                    .layers(1);

            final var pFramebuffer = stack.mallocLong(1);
            final var result = vkCreateFramebuffer(this.device, createInfo, null, pFramebuffer);
            if (result != VK_SUCCESS) {
                throw new IllegalStateException("Creating framebuffer failed: "
                                                + translateVulkanResult(result));
            }
            return pFramebuffer.get(0);
        }
    }

    @Override
    public void close() {
        for (final var handle : this.framebuffers) {
            vkDestroyFramebuffer(this.device, handle, null);
        }
    }
}
