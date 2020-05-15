package fi.jakojaannos.konna.vulkan.window;

import fi.jakojaannos.konna.vulkan.VulkanInstance;

import static fi.jakojaannos.konna.util.VkUtil.ensureSuccess;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;

public class WindowSurface implements AutoCloseable {
    private final VulkanInstance instance;
    private final long handle;

    public long getHandle() {
        return this.handle;
    }

    public WindowSurface(final VulkanInstance instance, final Window window) {
        this.instance = instance;

        try (final var stack = stackPush()) {
            final var pSurface = stack.mallocLong(1);
            ensureSuccess(glfwCreateWindowSurface(this.instance.getHandle(),
                                                  window.getHandle(),
                                                  null,
                                                  pSurface),
                          "Creating window surface failed");
            this.handle = pSurface.get(0);
        }
    }

    @Override
    public void close() {
        vkDestroySurfaceKHR(this.instance.getHandle(), this.handle, null);
    }
}
