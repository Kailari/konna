package fi.jakojaannos.konna.engine.application;

import fi.jakojaannos.konna.engine.vulkan.RenderingBackend;
import fi.jakojaannos.konna.engine.vulkan.window.Window;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;

public record Application(
        Window window,
        RenderingBackend backend
) implements AutoCloseable {
    public static Application initialize(
            final int windowWidth,
            final int windowHeight
    ) {
        if (!glfwInit()) {
            throw new IllegalStateException("Initializing GLFW failed!");
        }
        if (!glfwVulkanSupported()) {
            throw new IllegalStateException("No vulkan loader available.");
        }

        final var window = new Window(windowWidth, windowHeight);
        final var backend = RenderingBackend.create(window);

        return new Application(window, backend);
    }

    @Override
    public void close() {
        this.backend.close();
        this.window.close();
    }
}
