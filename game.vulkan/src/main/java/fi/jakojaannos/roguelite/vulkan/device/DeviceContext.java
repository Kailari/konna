package fi.jakojaannos.roguelite.vulkan.device;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.jakojaannos.roguelite.util.BufferUtil;

import static fi.jakojaannos.roguelite.util.VkUtil.translateVulkanResult;
import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class DeviceContext implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceContext.class);

    private final VkPhysicalDevice physicalDevice;
    private final VkDevice device;

    private final VkQueue graphicsQueue;
    private final VkQueue transferQueue;
    private final VkQueue presentQueue;
    private final QueueFamilies queueFamilies;

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

    public VkPhysicalDevice getPhysicalDevice() {
        return this.physicalDevice;
    }

    public QueueFamilies getQueueFamilies() {
        return this.queueFamilies;
    }

    public DeviceContext(
            final VkPhysicalDevice physicalDevice,
            final QueueFamilies queueFamilies,
            final PointerBuffer pExtensions
    ) {
        this.physicalDevice = physicalDevice;
        this.queueFamilies = queueFamilies;

        BufferUtil.forEachAsStringUTF8(pExtensions,
                                       name -> LOG.info("-> Device extension: {}", name));

        try (final var stack = stackPush()) {
            final var queueCreateInfos = createQueueCreateInfos(queueFamilies);

            final var deviceFeatures = VkPhysicalDeviceFeatures.callocStack();

            final var createInfo = VkDeviceCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                    .pQueueCreateInfos(queueCreateInfos)
                    .pEnabledFeatures(deviceFeatures)
                    .ppEnabledExtensionNames(pExtensions);

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
