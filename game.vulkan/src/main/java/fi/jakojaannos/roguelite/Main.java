package fi.jakojaannos.roguelite;

import fi.jakojaannos.roguelite.device.DeviceContext;
import fi.jakojaannos.roguelite.device.PhysicalDeviceSelector;

import static fi.jakojaannos.roguelite.util.VkUtil.translateVulkanResult;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public class Main {
    private VulkanInstance instance;
    private long surface;
    private DeviceContext deviceContext;

    public static void main(final String[] args) {
        try (final var window = new Window(800, 600)) {
            new Main().run(window);
        }
    }

    private void run(final Window window) {
        initVulkan(window);
        mainLoop(window);
        cleanup();
    }

    public void mainLoop(final Window window) {
        window.show();

        while (window.isOpen()) {
            glfwPollEvents();
        }
    }

    private void initVulkan(final Window window) {
        createInstance();
        createSurface(window);
        createDeviceContext();
    }

    private void createSurface(final Window window) {
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
            this.surface = pSurface.get(0);
        }
    }

    private void createDeviceContext() {
        try (final var stack = stackPush()) {
            final var candidate = PhysicalDeviceSelector.pickPhysicalDevice(this.instance,
                                                                            stack.mallocPointer(0),
                                                                            this.surface);
            this.deviceContext = new DeviceContext(candidate.physicalDevice(), candidate.queueFamilies());
        }
    }

    private void createInstance() {
        try (final var stack = stackPush()) {
            final var pValidationLayers = stack.pointers(
                    stack.UTF8("VK_LAYER_KHRONOS_validation"),
                    stack.UTF8("VK_LAYER_LUNARG_standard_validation")
            );
            final var pRequired = glfwGetRequiredInstanceExtensions();
            if (pRequired == null) {
                throw new IllegalStateException("GLFW could not figure out required extensions!");
            }
            final var pExtensionNames = stack.mallocPointer(pRequired.remaining() + 1);
            pExtensionNames.put(pRequired);
            pExtensionNames.put(stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME));
            pExtensionNames.flip();

            this.instance = new VulkanInstance(pValidationLayers, pExtensionNames);
        }
    }

    private void cleanup() {
        this.deviceContext.close();

        vkDestroySurfaceKHR(this.instance.getHandle(), this.surface, null);
        this.instance.close();
    }
}
