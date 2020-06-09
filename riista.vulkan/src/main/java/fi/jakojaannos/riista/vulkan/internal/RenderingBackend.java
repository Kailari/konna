package fi.jakojaannos.riista.vulkan.internal;

import fi.jakojaannos.riista.vulkan.internal.device.DeviceContext;
import fi.jakojaannos.riista.vulkan.internal.device.PhysicalDeviceSelector;
import fi.jakojaannos.riista.vulkan.rendering.Swapchain;
import fi.jakojaannos.riista.vulkan.internal.window.Window;
import fi.jakojaannos.riista.vulkan.internal.window.WindowSurface;

import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;

public record RenderingBackend(
        VulkanInstance instance,
        WindowSurface surface,
        DeviceContext deviceContext,
        Swapchain swapchain
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
        final var swapchain = new Swapchain(deviceContext, window, surface);

        return new RenderingBackend(instance, surface, deviceContext, swapchain);
    }

    public RenderingBackend {
        this.instance = instance;
        this.surface = surface;
        this.deviceContext = deviceContext;
        this.swapchain = swapchain;

        tryRecreate();
    }

    public void tryRecreate() {
        this.swapchain.tryRecreate();
    }

    @Override
    public void close() {
        this.swapchain.close();

        this.deviceContext.close();
        this.surface.close();
        this.instance.close();
    }
}
