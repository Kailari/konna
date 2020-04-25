package fi.jakojaannos.roguelite.device;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Comparator;

import fi.jakojaannos.roguelite.VulkanInstance;

import static fi.jakojaannos.roguelite.util.VkUtil.translateVulkanResult;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.VK10.*;

public class PhysicalDeviceSelector {
    private static final Logger LOG = LoggerFactory.getLogger(PhysicalDeviceSelector.class);

    private PhysicalDeviceSelector() {
    }

    public static DeviceCandidate pickPhysicalDevice(
            final VulkanInstance instance,
            final PointerBuffer pRequiredExtensions,
            final long surface
    ) {
        final var devices = getPhysicalDevices(instance.getHandle());
        return pickBest(instance.getHandle(), devices, pRequiredExtensions, surface);
    }

    private static long[] getPhysicalDevices(final VkInstance instance) {
        final long[] physicalDevices;
        try (final var stack = stackPush()) {
            final var pCount = stack.mallocInt(1);
            final var countResult = vkEnumeratePhysicalDevices(instance, pCount, null);
            if (countResult != VK_SUCCESS) {
                throw new IllegalStateException("Getting physical device count failed: "
                                                + translateVulkanResult(countResult));
            }

            final var pDevices = stack.mallocPointer(pCount.get(0));
            final var queryResult = vkEnumeratePhysicalDevices(instance, pCount, pDevices);
            if (queryResult != VK_SUCCESS) {
                throw new IllegalStateException("Querying physical devices failed: "
                                                + translateVulkanResult(countResult));
            }

            physicalDevices = new long[pCount.get(0)];
            pDevices.get(physicalDevices);
        }

        return physicalDevices;
    }

    private static DeviceCandidate pickBest(
            final VkInstance instance,
            final long[] devices,
            final PointerBuffer pRequiredExtensions,
            final long surface
    ) {
        final var candidates = new InternalDeviceCandidate[devices.length];
        for (int i = 0; i < devices.length; i++) {
            final var device = new VkPhysicalDevice(devices[i], instance);
            final var builder = QueueFamilies.builder();
            findDeviceQueueFamilies(device, builder, surface);

            final int suitability = rateDeviceSuitability(device, builder, pRequiredExtensions);
            candidates[i] = new InternalDeviceCandidate(device, builder.build(), suitability);
            LOG.debug("Device #{}: graphics={}, transfer={}, present={}",
                      i,
                      candidates[i].queueFamilies.graphics(),
                      candidates[i].queueFamilies.transfer(),
                      candidates[i].queueFamilies.present());
        }

        Arrays.sort(candidates,
                    Comparator.comparingInt(InternalDeviceCandidate::suitability)
                              .reversed());

        return new DeviceCandidate(candidates[0].physicalDevice,
                                   candidates[0].queueFamilies);
    }

    private static void findDeviceQueueFamilies(
            final VkPhysicalDevice device,
            final QueueFamilies.Builder builder,
            final long surface
    ) {
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
    }

    private static void checkPresentSupport(
            final VkPhysicalDevice device,
            final int familyIndex,
            final long surface,
            final IntBuffer pSupported
    ) {
        vkGetPhysicalDeviceSurfaceSupportKHR(device, familyIndex, surface, pSupported);
    }

    private static int rateDeviceSuitability(
            final VkPhysicalDevice device,
            final QueueFamilies.Builder builder,
            final PointerBuffer pRequiredExtensions
    ) {
        int suitability = 0;
        suitability += queueSuitability(builder);
        suitability += extensionSuitability(device, pRequiredExtensions);
        suitability += devicePropertySuitability(device);
        suitability += deviceFeatureSuitability(device);
        return suitability;
    }

    private static int queueSuitability(final QueueFamilies.Builder builder) {
        int suitability = 0;
        if (builder.hasSeparateGraphicsQueue()) {
            suitability += 100;
        }
        if (builder.hasSeparatePresentQueue()) {
            suitability += 200;
        }
        if (builder.hasSeparateTransferQueue()) {
            suitability += 50;
        }

        if (builder.isIncomplete()) {
            suitability -= 100_000;
        }
        return suitability;
    }

    private static int extensionSuitability(final VkPhysicalDevice device, final PointerBuffer pRequiredExtensions) {
        return 0;
    }

    private static int devicePropertySuitability(final VkPhysicalDevice device) {
        return 0;
    }

    private static int deviceFeatureSuitability(final VkPhysicalDevice device) {
        return 0;
    }

    public static record DeviceCandidate(
            VkPhysicalDevice physicalDevice,
            QueueFamilies queueFamilies
    ) {}

    private static record InternalDeviceCandidate(
            VkPhysicalDevice physicalDevice,
            QueueFamilies queueFamilies,
            int suitability
    ) {}
}
