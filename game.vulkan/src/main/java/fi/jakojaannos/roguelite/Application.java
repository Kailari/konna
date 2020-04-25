package fi.jakojaannos.roguelite;

import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.device.PhysicalDeviceSelector;
import fi.jakojaannos.roguelite.vulkan.window.Window;
import fi.jakojaannos.roguelite.vulkan.window.WindowSurface;

import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;


public record Application(
        Window window,
        VulkanInstance vulkanInstance,
        WindowSurface surface,
        DeviceContext deviceContext
) implements AutoCloseable {
    public static Application initialize() {
        final var window = new Window(800, 600);
        final var instance = createInstance();
        final var surface = new WindowSurface(instance, window);

        final var deviceContext = createDeviceContext(instance, surface);

        return new Application(window, instance, surface, deviceContext);
    }

    @Override
    public void close() {
        this.deviceContext.close();
        this.window.close();
        this.vulkanInstance.close();
    }

    private static VulkanInstance createInstance() {
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

            return new VulkanInstance(pValidationLayers, pExtensionNames);
        }
    }

    private static DeviceContext createDeviceContext(final VulkanInstance instance, final WindowSurface surface) {
        final PhysicalDeviceSelector.DeviceCandidate deviceCandidate;
        try (final var stack = stackPush()) {
            final var pExtensions = stack.mallocPointer(0);

            deviceCandidate = PhysicalDeviceSelector.pickPhysicalDevice(instance, pExtensions, surface);
        }
        return new DeviceContext(deviceCandidate.physicalDevice(), deviceCandidate.queueFamilies());
    }
}
