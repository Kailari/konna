package fi.jakojaannos.roguelite.vulkan.rendering;

import org.lwjgl.vulkan.*;

import fi.jakojaannos.roguelite.util.RecreateCloseable;
import fi.jakojaannos.roguelite.vulkan.command.CommandBuffer;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.types.VkFormat;

import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class RenderPass extends RecreateCloseable {
    private static final int COLOR_ATTACHMENT_INDEX = 0;

    private final VkDevice device;
    private final Swapchain swapchain;

    private VkFormat swapchainImageFormat;
    private long handle;

    public long getHandle() {
        return this.handle;
    }

    @Override
    protected boolean isRecreateRequired() {
        return this.swapchain.getImageFormat() != this.swapchainImageFormat;
    }

    public RenderPass(final DeviceContext deviceContext, final Swapchain swapchain) {
        this.device = deviceContext.getDevice();
        this.swapchain = swapchain;

        this.swapchainImageFormat = null;
        tryRecreate();
    }

    @Override
    protected void recreate() {
        this.swapchainImageFormat = this.swapchain.getImageFormat();
        try (final var stack = stackPush()) {
            final var attachments = VkAttachmentDescription.callocStack(1);
            attachments.get(COLOR_ATTACHMENT_INDEX)
                       .format(this.swapchainImageFormat.asInt())
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

            final var dependencies = VkSubpassDependency
                    .callocStack(1)
                    .srcSubpass(VK_SUBPASS_EXTERNAL)
                    .dstSubpass(0)
                    .srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                    .srcAccessMask(0)
                    .dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                    .dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

            final var createInfo = VkRenderPassCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                    .pAttachments(attachments)
                    .pSubpasses(subpasses)
                    .pDependencies(dependencies);
            final var pRenderPass = stack.mallocLong(1);
            ensureSuccess(vkCreateRenderPass(this.device, createInfo, null, pRenderPass),
                          "Creating render pass failed");
            this.handle = pRenderPass.get(0);
        }
    }

    @Override
    protected void cleanup() {
        vkDestroyRenderPass(this.device, this.handle, null);
    }

    public Scope begin(final Framebuffer framebuffer, final CommandBuffer commandBuffer) {
        return new Scope(this, framebuffer, commandBuffer);
    }

    public static final class Scope implements AutoCloseable {
        private final CommandBuffer commandBuffer;

        private Scope(
                final RenderPass renderPass,
                final Framebuffer framebuffer,
                final CommandBuffer commandBuffer
        ) {
            this.commandBuffer = commandBuffer;
            try (final var ignored = stackPush()) {
                final var beginInfo = VkRenderPassBeginInfo
                        .callocStack()
                        .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                        .renderPass(renderPass.getHandle())
                        .framebuffer(framebuffer.getHandle());

                beginInfo.renderArea()
                         .offset(VkOffset2D.callocStack().set(0, 0))
                         .extent(framebuffer.getExtent());

                final var clearValues = VkClearValue.callocStack(1);
                clearValues.get(0).color().float32(0, 0.0f);
                clearValues.get(0).color().float32(1, 0.0f);
                clearValues.get(0).color().float32(2, 0.0f);
                clearValues.get(0).color().float32(3, 1.0f);

                beginInfo.pClearValues(clearValues);

                vkCmdBeginRenderPass(this.commandBuffer.getHandle(),
                                     beginInfo,
                                     VK_SUBPASS_CONTENTS_INLINE);
            }
        }

        @Override
        public void close() {
            vkCmdEndRenderPass(this.commandBuffer.getHandle());
        }
    }
}
