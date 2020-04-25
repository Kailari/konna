package fi.jakojaannos.roguelite.vulkan.device;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;

import fi.jakojaannos.roguelite.VulkanInstance;
import fi.jakojaannos.roguelite.vulkan.window.WindowSurface;

import static fi.jakojaannos.roguelite.util.VkUtil.translateVulkanResult;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkEnumeratePhysicalDevices;

public class PhysicalDeviceSelector {
    private static final Logger LOG = LoggerFactory.getLogger(PhysicalDeviceSelector.class);

    private PhysicalDeviceSelector() {
    }

    public static DeviceCandidate pickPhysicalDevice(
            final VulkanInstance instance,
            final PointerBuffer pRequiredExtensions,
            final WindowSurface surface
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
            final WindowSurface surface
    ) {
        final var candidates = new InternalDeviceCandidate[devices.length];
        for (int i = 0; i < devices.length; i++) {
            final var device = new VkPhysicalDevice(devices[i], instance);
            final var queueFamilies = QueueFamilySelector.findDeviceQueueFamilies(device, surface);

            final int suitability = rateDeviceSuitability(device, queueFamilies, pRequiredExtensions);
            candidates[i] = new InternalDeviceCandidate(device, queueFamilies, suitability);
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

    private static int rateDeviceSuitability(
            final VkPhysicalDevice device,
            final QueueFamilies builder,
            final PointerBuffer pRequiredExtensions
    ) {
        int suitability = 0;
        suitability += queueSuitability(builder);
        suitability += extensionSuitability(device, pRequiredExtensions);
        suitability += devicePropertySuitability(device);
        suitability += deviceFeatureSuitability(device);
        return suitability;
    }

    private static int queueSuitability(final QueueFamilies queueFamilies) {
        int suitability = 0;
        if (queueFamilies.hasSeparateGraphicsQueue()) {
            suitability += 100;
        }
        if (queueFamilies.hasSeparatePresentQueue()) {
            suitability += 200;
        }
        if (queueFamilies.hasSeparateTransferQueue()) {
            suitability += 50;
        }

        if (queueFamilies.isIncomplete()) {
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
