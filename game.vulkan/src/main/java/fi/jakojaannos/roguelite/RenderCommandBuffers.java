package fi.jakojaannos.roguelite;

import fi.jakojaannos.roguelite.vulkan.CommandBuffer;
import fi.jakojaannos.roguelite.vulkan.CommandPool;
import fi.jakojaannos.roguelite.vulkan.rendering.Framebuffers;
import fi.jakojaannos.roguelite.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.roguelite.vulkan.rendering.RenderPass;

import static org.lwjgl.vulkan.VK10.*;

public class RenderCommandBuffers {
    private final CommandBuffer[] commandBuffers;

    public RenderCommandBuffers(
            final CommandPool graphicsCommandPool,
            final int swapchainImageCount,
            final RenderPass renderPass,
            final Framebuffers framebuffers,
            final GraphicsPipeline graphicsPipeline
    ) {
        this.commandBuffers = graphicsCommandPool.allocate(swapchainImageCount);

        for (int i = 0; i < this.commandBuffers.length; i++) {
            final var commandBuffer = this.commandBuffers[i];
            final var framebuffer = framebuffers.get(i);

            try (final var ignored = commandBuffer.begin();
                 final var ignored2 = renderPass.begin(framebuffer, commandBuffer)
            ) {
                vkCmdBindPipeline(commandBuffer.getHandle(),
                                  VK_PIPELINE_BIND_POINT_GRAPHICS,
                                  graphicsPipeline.getHandle());

                vkCmdDraw(commandBuffer.getHandle(), 3, 1, 0, 0);
            }
        }
    }

    public CommandBuffer get(final int imageIndex) {
        return this.commandBuffers[imageIndex];
    }
}
