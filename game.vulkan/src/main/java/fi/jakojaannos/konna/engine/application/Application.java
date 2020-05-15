package fi.jakojaannos.konna.engine.application;

import java.nio.file.Path;

import fi.jakojaannos.konna.engine.vulkan.RenderingContext;
import fi.jakojaannos.konna.engine.vulkan.RenderingBackend;
import fi.jakojaannos.konna.engine.vulkan.window.Window;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;


public record Application(
        Window window,
        RenderingBackend backend,
        RenderingContext renderingContext
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
        final var renderer = new RenderingContext(assetRoot, backend, window);

        return new Application(window, backend, renderer);
    }

    public void recreateSwapchain() {
        this.renderingContext.recreateSwapchain();
    }

    @Override
    public void close() {
        this.renderingContext.close();
        this.backend.close();
        this.window.close();
    }
}
