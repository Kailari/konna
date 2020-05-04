package fi.jakojaannos.roguelite.vulkan;

import org.joml.Vector3f;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.vulkan.command.CommandBuffer;
import fi.jakojaannos.roguelite.vulkan.command.CommandPool;
import fi.jakojaannos.roguelite.vulkan.rendering.Framebuffers;
import fi.jakojaannos.roguelite.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.roguelite.vulkan.rendering.RenderPass;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;
import fi.jakojaannos.roguelite.vulkan.window.Window;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class Renderer implements AutoCloseable {
    private final Swapchain swapchain;
    private final RenderPass renderPass;
    private final GraphicsPipeline<Vertex> graphicsPipeline;
    private final Framebuffers framebuffers;
    private final CommandPool commandPool;
    private final Mesh mesh;
    private CommandBuffer[] commandBuffers;

    public int getSwapchainImageCount() {
        return this.swapchain.getImageCount();
    }

    public Swapchain getSwapchain() {
        return this.swapchain;
    }

    public Renderer(final Path assetRoot, final RenderingBackend backend, final Window window) {
        this.swapchain = new Swapchain(backend.deviceContext(), window, backend.surface());

        this.renderPass = new RenderPass(backend.deviceContext(), this.swapchain);
        this.framebuffers = new Framebuffers(backend.deviceContext(),
                                             this.swapchain,
                                             this.renderPass);
        this.graphicsPipeline = new GraphicsPipeline<>(assetRoot,
                                                       backend.deviceContext(),
                                                       this.swapchain,
                                                       this.renderPass,
                                                       Vertex.FORMAT);

        this.commandPool = new CommandPool(backend.deviceContext().getDevice(),
                                           backend.deviceContext()
                                                  .getQueueFamilies()
                                                  .graphics());

        this.mesh = new Mesh(backend.deviceContext(),
                             new Vertex[]{
                                     new Vertex(new Vector3f(0.0f, -0.5f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f)),
                                     new Vertex(new Vector3f(0.5f, 0.5f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f)),
                                     new Vertex(new Vector3f(-0.5f, 0.5f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f)),
                             },
                             new Short[]{
                                     0, 1, 2
                             });

        recordCommandBuffers();
    }

    public CommandBuffer getCommands(final int imageIndex) {
        return this.commandBuffers[imageIndex];
    }

    public void recreateSwapchain() {
        freeCommandBuffers();

        this.swapchain.tryRecreate();
        this.renderPass.tryRecreate();
        this.graphicsPipeline.tryRecreate();
        this.framebuffers.tryRecreate();

        recordCommandBuffers();
    }

    private void recordCommandBuffers() {
        this.commandBuffers = this.commandPool.allocate(this.swapchain.getImageCount());
        for (int i = 0; i < this.commandBuffers.length; i++) {
            final var commandBuffer = this.commandBuffers[i];
            final var framebuffer = this.framebuffers.get(i);

            try (final var stack = stackPush();
                 final var ignored = commandBuffer.begin();
                 final var ignored2 = this.renderPass.begin(framebuffer, commandBuffer)
            ) {
                vkCmdBindPipeline(commandBuffer.getHandle(),
                                  VK_PIPELINE_BIND_POINT_GRAPHICS,
                                  this.graphicsPipeline.getHandle());

                vkCmdBindVertexBuffers(commandBuffer.getHandle(),
                                       0,
                                       stack.longs(this.mesh.getVertexBuffer().getHandle()),
                                       stack.longs(0L));
                vkCmdBindIndexBuffer(commandBuffer.getHandle(),
                                     this.mesh.getIndexBuffer().getHandle(),
                                     0,
                                     VK_INDEX_TYPE_UINT16);

                vkCmdDrawIndexed(commandBuffer.getHandle(),
                                 this.mesh.getIndexCount(),
                                 1,
                                 0,
                                 0,
                                 0);
            }
        }
    }

    private void freeCommandBuffers() {
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

    @Override
    public void close() {
        this.mesh.close();

        this.commandPool.close();
        this.framebuffers.close();
        this.renderPass.close();
        this.graphicsPipeline.close();

        this.swapchain.close();
    }
}
