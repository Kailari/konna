package fi.jakojaannos.roguelite.vulkan.window;

import fi.jakojaannos.roguelite.VulkanInstance;

import static fi.jakojaannos.roguelite.util.VkUtil.translateVulkanResult;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

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
            final var result = glfwCreateWindowSurface(this.instance.getHandle(),
                                                       window.getHandle(),
                                                       null,
                                                       pSurface);
            if (result != VK_SUCCESS) {
                throw new IllegalStateException("Creating window surface failed: "
                                                + translateVulkanResult(result));
            }
            this.handle = pSurface.get(0);
        }
    }

    @Override
    public void close() {
        vkDestroySurfaceKHR(this.instance.getHandle(), this.handle, null);
    }
}
