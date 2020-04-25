package fi.jakojaannos.roguelite.vulkan.rendering;

import org.lwjgl.vulkan.*;

import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;

import static fi.jakojaannos.roguelite.util.VkUtil.translateVulkanResult;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class RenderPass implements AutoCloseable {
    private static final int COLOR_ATTACHMENT_INDEX = 0;

    private final VkDevice device;
    private final long handle;

    public long getHandle() {
        return this.handle;
    }

    public RenderPass(final DeviceContext deviceContext, final int swapchainImageFormat) {
        this.device = deviceContext.getDevice();

        try (final var stack = stackPush()) {
            final var attachments = VkAttachmentDescription.callocStack(1);
            attachments.get(COLOR_ATTACHMENT_INDEX)
                       .format(swapchainImageFormat)
                       .samples(VK_SAMPLE_COUNT_1_BIT)
                       .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                       .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
                       .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                       .stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                       .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                       .finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

            final var colorAttachmentRef = VkAttachmentReference
                    .callocStack(1)
                    .attachment(COLOR_ATTACHMENT_INDEX)
                    .layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            final var subpasses = VkSubpassDescription
                    .callocStack(1)
                    .pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
                    .colorAttachmentCount(1)
                    .pColorAttachments(colorAttachmentRef);

            final var createInfo = VkRenderPassCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                    .pAttachments(attachments)
                    .pSubpasses(subpasses);
            final var pRenderPass = stack.mallocLong(1);
            final var result = vkCreateRenderPass(this.device, createInfo, null, pRenderPass);
            if (result != VK_SUCCESS) {
                throw new IllegalStateException("Creating render pass failed: "
                                                + translateVulkanResult(result));
            }
            this.handle = pRenderPass.get(0);
        }
    }

    @Override
    public void close() {
        vkDestroyRenderPass(this.device, this.handle, null);
    }
}
