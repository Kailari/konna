package fi.jakojaannos.konna;

import java.nio.file.Path;

import fi.jakojaannos.konna.vulkan.Renderer;
import fi.jakojaannos.konna.vulkan.RenderingBackend;
import fi.jakojaannos.konna.vulkan.window.Window;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;


public record Application(
        Window window,
        RenderingBackend backend,
        Renderer renderer
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
        final var renderer = new Renderer(assetRoot, backend, window);

        return new Application(window, backend, renderer);
    }

    public void recreateSwapchain() {
        this.renderer.recreateSwapchain();
    }

    @Override
    public void close() {
        this.renderer.close();
        this.backend.close();
        this.window.close();
    }
}
