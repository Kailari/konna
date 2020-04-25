package fi.jakojaannos.roguelite.vulkan.device;

import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.nio.IntBuffer;

import fi.jakojaannos.roguelite.vulkan.window.WindowSurface;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.VK10.*;

public class QueueFamilySelector {
    private QueueFamilySelector() {
    }

    public static QueueFamilies findDeviceQueueFamilies(
            final VkPhysicalDevice device,
            final WindowSurface surface
    ) {
        final var builder = QueueFamilies.builder();
        try (final var stack = stackPush()) {
            final var pCount = stack.mallocInt(1);
            vkGetPhysicalDeviceQueueFamilyProperties(device, pCount, null);

            final var familyCount = pCount.get(0);
            final var pQueueFamilies = VkQueueFamilyProperties.callocStack(familyCount);
            vkGetPhysicalDeviceQueueFamilyProperties(device, pCount, pQueueFamilies);

            // Note: allocate temporary pointer here to avoid native memory allocations in loop
            final var pSupported = stack.mallocInt(1);

            // Iterate the queue families and try to find all desired queues
            int selectedGraphics = -1;
            int selectedTransfer = -1;
            int selectedPresent = -1;
            for (int i = 0; i < familyCount; i++) {
                final var props = pQueueFamilies.get(i);
                final var hasGraphicsBit = (props.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0;
                if (selectedTransfer == -1 && hasGraphicsBit) {
                    selectedGraphics = i;
                }

                final var hasTransferBit = (props.queueFlags() & VK_QUEUE_TRANSFER_BIT) != 0;
                if (selectedTransfer == -1 && hasTransferBit && selectedGraphics != i) {
                    selectedTransfer = i;
                }

                checkPresentSupport(device, i, surface, pSupported);
                if (pSupported.get(0) == VK_TRUE && selectedGraphics != i) {
                    selectedPresent = i;
                }

                final var allFound = (selectedGraphics != -1 && selectedTransfer != -1 && selectedPresent != -1);
                if (allFound) {
                    break;
                }
            }

            // Fallback transfer queue to graphics queue if separate queue is not found
            if (selectedTransfer == -1 && selectedGraphics != -1) {
                final var graphicsProps = pQueueFamilies.get(selectedGraphics);
                if ((graphicsProps.queueFlags() & VK_QUEUE_TRANSFER_BIT) != 0) {
                    selectedTransfer = selectedGraphics;
                }
            }

            // Fallback present queue to graphics queue if separate queue is not found
            if (selectedPresent == -1 && selectedGraphics != -1) {
                checkPresentSupport(device, selectedGraphics, surface, pSupported);
                if (pSupported.get(0) == VK_TRUE) {
                    selectedPresent = selectedGraphics;
                }
            }

            builder.graphics(selectedGraphics);
            builder.transfer(selectedTransfer);
            builder.present(selectedPresent);
        }

        return builder.build();
    }

    private static void checkPresentSupport(
            final VkPhysicalDevice device,
            final int familyIndex,
            final WindowSurface surface,
            final IntBuffer pSupported
    ) {
        vkGetPhysicalDeviceSurfaceSupportKHR(device, familyIndex, surface.getHandle(), pSupported);
    }
}
