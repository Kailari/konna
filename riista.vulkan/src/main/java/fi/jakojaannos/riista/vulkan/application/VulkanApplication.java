package fi.jakojaannos.riista.vulkan.application;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.jakojaannos.riista.application.Application;
import fi.jakojaannos.riista.vulkan.internal.RenderingBackend;
import fi.jakojaannos.riista.vulkan.internal.window.Window;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;

public record VulkanApplication(
        Window window,
        RenderingBackend backend
) implements Application {
    private static final Logger LOG = LoggerFactory.getLogger(VulkanApplication.class);

    public static VulkanApplication initialize(
            final int windowWidth,
            final int windowHeight
    ) {
        glfwSetErrorCallback(VulkanApplication::logGLFWError);

        if (!glfwInit()) {
            throw new IllegalStateException("Initializing GLFW failed!");
        }
        if (!glfwVulkanSupported()) {
            throw new IllegalStateException("No vulkan loader available.");
        }

        final var window = new Window(windowWidth, windowHeight);
        final var backend = RenderingBackend.create(window);

        return new VulkanApplication(window, backend);
    }

    private static void logGLFWError(final int error, final long description) {
        LOG.error("GLFW Error: [{}] {}",
                  error,
                  GLFWErrorCallback.getDescription(description));
    }

    @Override
    public void close() {
        this.backend.close();
        this.window.close();
    }
}
