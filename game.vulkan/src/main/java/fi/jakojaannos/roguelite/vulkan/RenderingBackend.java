package fi.jakojaannos.roguelite.vulkan;

import fi.jakojaannos.roguelite.VulkanInstance;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.device.PhysicalDeviceSelector;
import fi.jakojaannos.roguelite.vulkan.window.Window;
import fi.jakojaannos.roguelite.vulkan.window.WindowSurface;

import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;

public record RenderingBackend(
        VulkanInstance instance,
        WindowSurface surface,
        DeviceContext deviceContext
) implements AutoCloseable {
    public static RenderingBackend create(final Window window) {
        final String[] instanceLayers = {
                "VK_LAYER_KHRONOS_validation",
                "VK_LAYER_LUNARG_standard_validation"
        };
        final String[] instanceExtensions = {
                VK_EXT_DEBUG_UTILS_EXTENSION_NAME
        };
        final String[] deviceExtensions = {
                VK_KHR_SWAPCHAIN_EXTENSION_NAME
        };

        final VulkanInstance instance = new VulkanInstance(instanceLayers, instanceExtensions);
        final var surface = new WindowSurface(instance, window);

        final var deviceCandidate = PhysicalDeviceSelector.pickPhysicalDevice(instance,
                                                                              deviceExtensions,
                                                                              surface);
        final var deviceContext = new DeviceContext(deviceCandidate.physicalDevice(),
                                                    deviceCandidate.queueFamilies(),
                                                    deviceExtensions);

        return new RenderingBackend(instance, surface, deviceContext);
    }

    @Override
    public void close() {
        this.deviceContext.close();
        this.surface.close();
        this.instance.close();
    }
}
