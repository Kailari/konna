package fi.jakojaannos.roguelite;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.vulkan.command.CommandPool;
import fi.jakojaannos.roguelite.vulkan.RenderingBackend;
import fi.jakojaannos.roguelite.vulkan.rendering.Framebuffers;
import fi.jakojaannos.roguelite.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.roguelite.vulkan.rendering.RenderPass;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;
import fi.jakojaannos.roguelite.vulkan.window.Window;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;


public record Application(
        Window window,
        RenderingBackend backend,
        Swapchain swapchain,
        RenderPass renderPass,
        GraphicsPipeline graphicsPipeline,
        Framebuffers framebuffers,
        CommandPool graphicsCommandPool
) implements AutoCloseable {
    public static Application initialize(final int windowWidth, final int windowHeight, final Path assetRoot) {
        if (!glfwInit()) {
            throw new IllegalStateException("Initializing GLFW failed!");
        }
        if (!glfwVulkanSupported()) {
            throw new IllegalStateException("No vulkan loader available.");
        }

        final var window = new Window(windowWidth, windowHeight);
        final var backend = RenderingBackend.create(window);
        final var swapchain = new Swapchain(backend.deviceContext(), window, backend.surface());

        final var renderPass = new RenderPass(backend.deviceContext(), swapchain);
        final var framebuffers = new Framebuffers(backend.deviceContext(),
                                                  swapchain,
                                                  renderPass);
        final var graphicsPipeline = new GraphicsPipeline(assetRoot,
                                                          backend.deviceContext(),
                                                          swapchain,
                                                          renderPass);

        final var graphicsCommandPool = new CommandPool(backend.deviceContext(),
                                                        backend.deviceContext()
                                                               .getQueueFamilies()
                                                               .graphics());

        return new Application(window,
                               backend,
                               swapchain,
                               renderPass,
                               graphicsPipeline,
                               framebuffers,
                               graphicsCommandPool);
    }

    public void recreateSwapchain() {
        this.swapchain.tryRecreate();
        this.renderPass.tryRecreate();
        this.graphicsPipeline.tryRecreate();
        this.framebuffers.tryRecreate();
    }

    @Override
    public void close() {
        this.graphicsCommandPool.close();
        this.framebuffers.close();
        this.renderPass.close();
        this.graphicsPipeline.close();

        this.swapchain.close();
        this.backend.close();
        this.window.close();
    }
}
