package fi.jakojaannos.roguelite.vulkan.device;

import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import fi.jakojaannos.roguelite.vulkan.window.WindowSurface;

import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public record SwapchainSupportDetails(
        VkSurfaceCapabilitiesKHR capabilities,
        VkSurfaceFormatKHR[]formats,
        int[]presentModes
) {
    public static SwapchainSupportDetails query(
            final VkPhysicalDevice device,
            final WindowSurface surface
    ) {
        final var capabilities = VkSurfaceCapabilitiesKHR.callocStack();
        vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface.getHandle(), capabilities);

        final var stack = stackGet();
        final var pCount = stack.mallocInt(1);
        final var countResult = vkGetPhysicalDeviceSurfaceFormatsKHR(device,
                                                                     surface.getHandle(),
                                                                     pCount,
                                                                     null);
        if (countResult != VK_SUCCESS) {
            // FIXME: Do not throw, discard the physical device instead. Same for other throws here
            throw new IllegalStateException("Querying device swapchain surface format count failed!");
        }

        final var surfaceFormatCount = pCount.get(0);
        final var pFormats = VkSurfaceFormatKHR.callocStack(surfaceFormatCount);
        if (surfaceFormatCount > 0) {
            final var queryResult = vkGetPhysicalDeviceSurfaceFormatsKHR(device,
                                                                         surface.getHandle(),
                                                                         pCount,
                                                                         pFormats);
            if (queryResult != VK_SUCCESS) {
                throw new IllegalStateException("Querying device swapchain surface formats failed!");
            }
        }
        final var formats = new VkSurfaceFormatKHR[surfaceFormatCount];
        for (int i = 0; i < surfaceFormatCount; i++) {
            formats[i] = pFormats.get(i);
        }

        final var presentCountResult = vkGetPhysicalDeviceSurfacePresentModesKHR(device,
                                                                                 surface.getHandle(),
                                                                                 pCount,
                                                                                 null);
        if (presentCountResult != VK_SUCCESS) {
            throw new IllegalStateException("Querying device swapchain present mode count failed!");
        }

        final var presentModeCount = pCount.get(0);
        final var pModes = stack.mallocInt(presentModeCount);
        if (presentModeCount > 0) {
            final var presentQueryResult = vkGetPhysicalDeviceSurfacePresentModesKHR(device,
                                                                                     surface.getHandle(),
                                                                                     pCount,
                                                                                     pModes);
            if (presentQueryResult != VK_SUCCESS) {
                throw new IllegalStateException("Querying device swapchain present modes failed!");
            }
        }
        final var presentModes = new int[presentModeCount];
        pModes.get(presentModes);

        return new SwapchainSupportDetails(capabilities, formats, presentModes);
    }
}
