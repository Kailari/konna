package fi.jakojaannos.roguelite.device;

import org.lwjgl.vulkan.*;

import static fi.jakojaannos.roguelite.util.VkUtil.translateVulkanResult;
import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class DeviceContext implements AutoCloseable {
    private final VkPhysicalDevice physicalDevice;
    private final VkDevice device;

    private final VkQueue graphicsQueue;
    private final VkQueue transferQueue;
    private final VkQueue presentQueue;

    public VkQueue getGraphicsQueue() {
        return this.graphicsQueue;
    }

    public VkQueue getTransferQueue() {
        return this.transferQueue;
    }

    public VkQueue getPresentQueue() {
        return this.presentQueue;
    }

    public VkDevice getDevice() {
        return this.device;
    }

    public DeviceContext(
            final VkPhysicalDevice physicalDevice,
            final QueueFamilies queueFamilies
    ) {
        this.physicalDevice = physicalDevice;

        try (final var stack = stackPush()) {
            final var queueCreateInfos = createQueueCreateInfos(queueFamilies);

            final var deviceFeatures = VkPhysicalDeviceFeatures.callocStack();

            final var createInfo = VkDeviceCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                    .pQueueCreateInfos(queueCreateInfos)
                    .pEnabledFeatures(deviceFeatures)
                    .ppEnabledExtensionNames(stack.mallocPointer(0));

            final var pDevice = stack.mallocPointer(1);
            final var result = vkCreateDevice(this.physicalDevice, createInfo, null, pDevice);
            if (result != VK_SUCCESS) {
                throw new IllegalStateException("Creating logical device failed: "
                                                + translateVulkanResult(result));
            }

            this.device = new VkDevice(pDevice.get(0), physicalDevice, createInfo);
        }

        this.graphicsQueue = getQueue(queueFamilies.graphics(), 0);
        this.transferQueue = getQueue(queueFamilies.transfer(), 0);
        this.presentQueue = getQueue(queueFamilies.present(), 0);
    }

    public VkQueue getQueue(final int queueFamilyIndex, final int index) {
        try (final var stack = stackPush()) {
            final var pQueue = stack.mallocPointer(1);
            vkGetDeviceQueue(this.device, queueFamilyIndex, index, pQueue);

            return new VkQueue(pQueue.get(0), this.device);
        }
    }

    public VkDeviceQueueCreateInfo.Buffer createQueueCreateInfos(final QueueFamilies queueFamilies) {
        final var stack = stackGet();

        final var uniqueIndices = queueFamilies.getUniqueIndices();
        final var infos = VkDeviceQueueCreateInfo.callocStack(uniqueIndices.size());
        var i = 0;
        for (final var index : uniqueIndices) {
            infos.get(i)
                 .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                 .queueFamilyIndex(index)
                 .pQueuePriorities(stack.floats(1.0f));

            ++i;
        }

        return infos;
    }

    @Override
    public void close() {
        vkDestroyDevice(this.device, null);
    }
}
