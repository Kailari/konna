package fi.jakojaannos.riista.vulkan.rendering;

import org.lwjgl.vulkan.*;

import java.util.*;

import fi.jakojaannos.riista.utilities.BitFlags;
import fi.jakojaannos.riista.utilities.BitMask;
import fi.jakojaannos.riista.vulkan.internal.RenderingBackend;
import fi.jakojaannos.riista.vulkan.internal.command.CommandBuffer;
import fi.jakojaannos.riista.vulkan.internal.types.VkAccessFlagBits;
import fi.jakojaannos.riista.vulkan.internal.types.VkPipelineStageFlagBits;
import fi.jakojaannos.riista.vulkan.renderer.DepthAttachment;
import fi.jakojaannos.riista.vulkan.renderer.RenderSubpass;
import fi.jakojaannos.riista.vulkan.util.RecreateCloseable;

import static fi.jakojaannos.riista.vulkan.util.VkUtil.ensureSuccess;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class RenderPass extends RecreateCloseable {
    private final RenderingBackend backend;

    private final Attachment[] attachments;
    private final RenderSubpass[] subpasses;
    private final SubpassDependencyInfo[] subpassDependencies;

    // Same as attachments, but compacted by removing nulls (array indices might not match what GPU uses)
    private final Attachment[] activeAttachments;

    private final int depthAttachmentIndex;

    private long handle;

    public long getHandle() {
        return this.handle;
    }

    @Override
    protected boolean isRecreateRequired() {
        return Arrays.stream(this.attachments)
                     .anyMatch(Attachment::isRecreateRequired);
    }

    public RenderPass(
            final RenderingBackend backend,
            final int depthAttachmentIndex,
            final Map<Integer, Attachment> attachments,
            final List<RenderSubpass> subpasses,
            final Collection<SubpassDependencyInfo> subpassDependencies
    ) {
        this.backend = backend;
        this.depthAttachmentIndex = depthAttachmentIndex;

        this.subpasses = subpasses.toArray(RenderSubpass[]::new);
        this.subpassDependencies = subpassDependencies.toArray(SubpassDependencyInfo[]::new);

        // Map the attachments from the by-index-lookup to single array. The result array may have nulls.
        final var maxAttachmentId = attachments.keySet()
                                               .stream()
                                               .mapToInt(Integer::intValue)
                                               .max()
                                               .orElse(0);
        this.attachments = new Attachment[maxAttachmentId + 1];
        for (final var entry : attachments.entrySet()) {
            this.attachments[entry.getKey()] = entry.getValue();
        }

        this.activeAttachments = attachments.values()
                                            .toArray(Attachment[]::new);
    }

    public static Builder builder(final RenderingBackend backend) {
        return new Builder(backend);
    }

    public boolean hasDepthAttachment() {
        return true;
    }

    @Override
    protected void recreate() {
        for (final var attachment : this.attachments) {
            attachment.onRecreate();
        }

        try (final var stack = stackPush()) {
            final var attachments = VkAttachmentDescription.callocStack(this.attachments.length);

            for (int i = 0; i < this.attachments.length; i++) {
                attachments.get(i)
                           .format(this.attachments[i].format().asInt())
                           .samples(this.attachments[i].samples().asInt())
                           .loadOp(this.attachments[i].loadOp())
                           .storeOp(this.attachments[i].storeOp())
                           .stencilLoadOp(this.attachments[i].stencilLoadOp())
                           .stencilStoreOp(this.attachments[i].stencilStoreOp())
                           .initialLayout(this.attachments[i].initialLayout())
                           .finalLayout(this.attachments[i].finalLayout());
            }

            final var subpasses = VkSubpassDescription.callocStack(this.subpasses.length);
            for (int i = 0; i < this.subpasses.length; i++) {
                final var subpass = this.subpasses[i];
                if (subpass.hasDepthAttachment() && this.depthAttachmentIndex == -1) {
                    throw new IllegalStateException("Tried creating render pass with one or more subpasses "
                                                    + "requiring a depth attachment, but the render pass "
                                                    + "does not have one!");
                }

                final var colorAttachments = subpass.colorAttachments();
                final var colorAttachmentRefs = VkAttachmentReference.callocStack(colorAttachments.length);
                for (int attachmentIndex = 0; attachmentIndex < colorAttachments.length; attachmentIndex++) {
                    final var attachment = colorAttachments[attachmentIndex];

                    colorAttachmentRefs.get(attachmentIndex)
                                       .attachment(List.of(this.attachments).indexOf(attachment))
                                       .layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
                }

                final var depthAttachmentRef = subpass.hasDepthAttachment()
                        ? VkAttachmentReference.callocStack()
                                               .attachment(this.depthAttachmentIndex)
                                               .layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
                        : null;

                // FIXME: Allow setting bind point for non-graphics shaders
                subpasses.get(i)
                         .pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
                         .colorAttachmentCount(subpass.colorAttachments().length)
                         .pColorAttachments(colorAttachmentRefs)
                         .pDepthStencilAttachment(depthAttachmentRef);
            }

            final var dependencies = VkSubpassDependency.callocStack(this.subpassDependencies.length);
            for (int i = 0; i < this.subpassDependencies.length; i++) {
                final var dependency = this.subpassDependencies[i];

                dependencies.get(i)
                            .srcSubpass(dependency.srcSubpass)
                            .dstSubpass(dependency.dstSubpass)
                            .srcStageMask(dependency.srcStageMask.mask())
                            .srcAccessMask(dependency.srcAccessMask.mask())
                            .dstStageMask(dependency.dstStageMask.mask())
                            .dstAccessMask(dependency.dstAccessMask.mask());
            }

            final var createInfo = VkRenderPassCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                    .pAttachments(attachments)
                    .pSubpasses(subpasses)
                    .pDependencies(dependencies);
            final var pRenderPass = stack.mallocLong(1);
            ensureSuccess(vkCreateRenderPass(this.backend.deviceContext().getDevice(),
                                             createInfo,
                                             null,
                                             pRenderPass),
                          "Creating render pass failed");
            this.handle = pRenderPass.get(0);
        }
    }

    @Override
    protected void cleanup() {
        vkDestroyRenderPass(this.backend.deviceContext().getDevice(),
                            this.handle,
                            null);
    }

    public Scope begin(final Framebuffer framebuffer, final CommandBuffer commandBuffer) {
        return new Scope(this, framebuffer, commandBuffer, this.activeAttachments);
    }

    public int indexOf(final RenderSubpass subpass) {
        return List.of(this.subpasses).indexOf(subpass);
    }

    public static final class Scope implements AutoCloseable {
        private final CommandBuffer commandBuffer;

        private Scope(
                final RenderPass renderPass,
                final Framebuffer framebuffer,
                final CommandBuffer commandBuffer,
                final Attachment[] activeAttachments
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

                final var clearValues = VkClearValue.callocStack(activeAttachments.length);
                for (int i = 0; i < activeAttachments.length; i++) {
                    activeAttachments[i].clearValue(clearValues.get(i));
                }
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

    public static class Builder {
        private final RenderingBackend backend;

        private final Map<Integer, Attachment> attachments = new HashMap<>();
        private final List<RenderSubpass> subpasses = new ArrayList<>();
        private final Collection<SubpassDependencyInfo> subpassDependencies = new ArrayList<>();
        private int depthAttachmentIndex = -1;

        public Builder(final RenderingBackend backend) {
            this.backend = backend;
        }

        public Builder withDepthAttachment(final int index) {
            this.depthAttachmentIndex = index;
            this.attachments.put(index, new DepthAttachment(this.backend.deviceContext()));
            return this;
        }

        public Builder colorAttachment(final int index, final Attachment attachment) {
            this.attachments.put(index, attachment);
            return this;
        }

        public Builder subpass(final RenderSubpass subpass) {
            this.subpasses.add(subpass);
            return this;
        }

        public Builder subpassDependency(
                final int srcSubpass,
                final RenderSubpass dstSubpass,
                final BitMask<VkPipelineStageFlagBits> srcStageMask,
                final BitMask<BitFlags> srcAccessMask,
                final BitMask<VkPipelineStageFlagBits> dstStageMask,
                final BitMask<VkAccessFlagBits> dstAccessMask
        ) {
            return subpassDependency(srcSubpass,
                                     this.subpasses.indexOf(dstSubpass),
                                     srcStageMask,
                                     srcAccessMask,
                                     dstStageMask,
                                     dstAccessMask);
        }

        public Builder subpassDependency(
                final RenderSubpass srcSubpass,
                final RenderSubpass dstSubpass,
                final BitMask<VkPipelineStageFlagBits> srcStageMask,
                final BitMask<BitFlags> srcAccessMask,
                final BitMask<VkPipelineStageFlagBits> dstStageMask,
                final BitMask<VkAccessFlagBits> dstAccessMask
        ) {
            return subpassDependency(this.subpasses.indexOf(srcSubpass),
                                     this.subpasses.indexOf(dstSubpass),
                                     srcStageMask,
                                     srcAccessMask,
                                     dstStageMask,
                                     dstAccessMask);
        }

        public Builder subpassDependency(
                final int srcSubpass,
                final int dstSubpass,
                final BitMask<VkPipelineStageFlagBits> srcStageMask,
                final BitMask<BitFlags> srcAccessMask,
                final BitMask<VkPipelineStageFlagBits> dstStageMask,
                final BitMask<VkAccessFlagBits> dstAccessMask
        ) {
            this.subpassDependencies.add(new SubpassDependencyInfo(srcSubpass,
                                                                   dstSubpass,
                                                                   srcStageMask,
                                                                   srcAccessMask,
                                                                   dstStageMask,
                                                                   dstAccessMask));
            return this;
        }

        public RenderPass build() {
            return new RenderPass(this.backend,
                                  this.depthAttachmentIndex,
                                  this.attachments,
                                  this.subpasses,
                                  this.subpassDependencies);
        }
    }

    private static record SubpassDependencyInfo(
            int srcSubpass,
            int dstSubpass,
            BitMask<VkPipelineStageFlagBits>srcStageMask,
            BitMask<BitFlags>srcAccessMask,
            BitMask<VkPipelineStageFlagBits>dstStageMask,
            BitMask<VkAccessFlagBits>dstAccessMask
    ) {}
}
