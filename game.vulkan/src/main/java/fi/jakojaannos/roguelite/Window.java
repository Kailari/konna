package fi.jakojaannos.roguelite;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window implements AutoCloseable {
    private final long handle;

    public boolean isOpen() {
        return !glfwWindowShouldClose(this.handle);
    }

    public long getHandle() {
        return this.handle;
    }

    public Window(final int width, final int height) {
        if (!glfwInit()) {
            throw new IllegalStateException("Initializing GLFW failed!");
        }
        if (!glfwVulkanSupported()) {
            throw new IllegalStateException("No vulkan loader available.");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        this.handle = glfwCreateWindow(width, height, "Hello Vulkan", NULL, NULL);
        glfwSetKeyCallback(this.handle, (win, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(win, true);
            }
        });
    }

    public void show() {
        glfwShowWindow(this.handle);
    }

    @Override
    public void close() {
        glfwDestroyWindow(this.handle);
        glfwTerminate();
    }
}
