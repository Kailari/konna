package fi.jakojaannos.roguelite;

import fi.jakojaannos.roguelite.vulkan.command.CommandBuffer;
import fi.jakojaannos.roguelite.vulkan.command.CommandPool;
import fi.jakojaannos.roguelite.vulkan.rendering.Framebuffers;
import fi.jakojaannos.roguelite.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.roguelite.vulkan.rendering.RenderPass;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class RenderCommandBuffers implements AutoCloseable {
    private final CommandBuffer[] commandBuffers;
    private final CommandPool commandPool;

    public RenderCommandBuffers(
            final CommandPool graphicsCommandPool,
            final int swapchainImageCount,
            final RenderPass renderPass,
            final Framebuffers framebuffers,
            final GraphicsPipeline graphicsPipeline
    ) {
        this.commandPool = graphicsCommandPool;
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

    @Override
    public void close() {
        try (final var stack = stackPush()) {
            final var pBuffers = stack.mallocPointer(this.commandBuffers.length);
            for (int i = 0; i < this.commandBuffers.length; i++) {
                pBuffers.put(i, this.commandBuffers[i].getHandle());
            }
            vkFreeCommandBuffers(this.commandPool.getDevice(),
                                 this.commandPool.getHandle(),
                                 pBuffers);
        }
    }
}
