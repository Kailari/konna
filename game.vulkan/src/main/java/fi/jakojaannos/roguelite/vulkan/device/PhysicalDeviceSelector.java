package fi.jakojaannos.roguelite.vulkan.device;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;

import fi.jakojaannos.roguelite.VulkanInstance;
import fi.jakojaannos.roguelite.util.BufferUtil;
import fi.jakojaannos.roguelite.vulkan.window.WindowSurface;

import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
import static fi.jakojaannos.roguelite.util.VkUtil.translateVulkanResult;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class PhysicalDeviceSelector {
    private static final Logger LOG = LoggerFactory.getLogger(PhysicalDeviceSelector.class);

    private PhysicalDeviceSelector() {
    }

    public static DeviceCandidate pickPhysicalDevice(
            final VulkanInstance instance,
            final String[] requiredExtensions,
            final WindowSurface surface
    ) {
        try (final var stack = stackPush()) {
            final var pRequiredExtensions = stack.pointers(Arrays.stream(requiredExtensions)
                                                                 .map(stack::UTF8)
                                                                 .toArray(ByteBuffer[]::new));

            final var devices = getPhysicalDevices(instance.getHandle());
            return pickBest(instance.getHandle(), devices, pRequiredExtensions, surface);
        }
    }

    private static long[] getPhysicalDevices(final VkInstance instance) {
        final long[] physicalDevices;
        try (final var stack = stackPush()) {
            final var pCount = stack.mallocInt(1);
            ensureSuccess(vkEnumeratePhysicalDevices(instance, pCount, null),
                          "Getting physical device count failed");

            final var pDevices = stack.mallocPointer(pCount.get(0));
            ensureSuccess(vkEnumeratePhysicalDevices(instance, pCount, pDevices),
                          "Querying physical devices failed");

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
        try (final var ignored = stackPush()) {
            final var candidates = new InternalDeviceCandidate[devices.length];
            for (int i = 0; i < devices.length; i++) {
                final var device = new VkPhysicalDevice(devices[i], instance);
                final var queueFamilies = QueueFamilySelector.findDeviceQueueFamilies(device, surface);
                final var swapchainSupport = SwapchainSupportDetails.query(device, surface);

                final int suitability = rateDeviceSuitability(device, queueFamilies, pRequiredExtensions, swapchainSupport);
                candidates[i] = new InternalDeviceCandidate(device, queueFamilies, suitability);
            }

            Arrays.sort(candidates,
                        Comparator.comparingInt(InternalDeviceCandidate::suitability)
                                  .reversed());

            return new DeviceCandidate(candidates[0].physicalDevice,
                                       candidates[0].queueFamilies);
        }
    }

    private static int rateDeviceSuitability(
            final VkPhysicalDevice device,
            final QueueFamilies builder,
            final PointerBuffer pRequiredExtensions,
            final SwapchainSupportDetails details
    ) {
        int suitability = 0;
        suitability += queueSuitability(builder);
        suitability += extensionSuitability(device, pRequiredExtensions);
        suitability += swapchainSuitability(details);
        suitability += devicePropertySuitability(device);
        suitability += deviceFeatureSuitability(device);
        return suitability;
    }

    private static int swapchainSuitability(final SwapchainSupportDetails swapchainSupport) {
        int suitability = 0;
        if (swapchainSupport.formats().length == 0 || swapchainSupport.presentModes().length == 0) {
            suitability -= 100_000;
        }

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
        int suitability = 0;
        try (final var stack = stackPush()) {
            final var pCount = stack.mallocInt(1);
            final var countResult = vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, pCount, null);
            if (countResult != VK_SUCCESS) {
                LOG.warn("Getting supported extension count for a physical device failed: {}",
                         translateVulkanResult(countResult));
                return -100_000;
            }

            final var pAvailable = VkExtensionProperties.callocStack(pCount.get(0));
            final var queryResult = vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, pCount, pAvailable);
            if (queryResult != VK_SUCCESS) {
                LOG.warn("Querying supported extensions for a physical device failed: {}",
                         translateVulkanResult(countResult));
                return -100_000;
            }

            final var allSupported = BufferUtil.filteredForEachAsStringUTF8(
                    pRequiredExtensions,
                    name -> pAvailable.stream()
                                      .map(VkExtensionProperties::extensionNameString)
                                      .noneMatch(name::equals),
                    notFound -> {});

            if (!allSupported) {
                suitability -= 100_000;
            }
        }

        return suitability;
    }

    private static int devicePropertySuitability(final VkPhysicalDevice device) {
        return 0;
    }

    private static int deviceFeatureSuitability(final VkPhysicalDevice device) {
        int suitability = 0;
        try (final var ignored = stackPush()) {
            final var features = VkPhysicalDeviceFeatures.callocStack();
            vkGetPhysicalDeviceFeatures(device, features);

            if (features.samplerAnisotropy()) {
                suitability += 100;
            }
        }

        return suitability;
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
