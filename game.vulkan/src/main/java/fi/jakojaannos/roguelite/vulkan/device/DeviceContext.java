package fi.jakojaannos.roguelite.vulkan.device;

import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import fi.jakojaannos.roguelite.util.BufferUtil;
import fi.jakojaannos.roguelite.vulkan.command.CommandPool;
import fi.jakojaannos.roguelite.vulkan.command.GPUQueue;
import fi.jakojaannos.roguelite.vulkan.memory.MemoryManager;
import fi.jakojaannos.roguelite.vulkan.memory.slice.SliceMemoryManager;

import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class DeviceContext implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceContext.class);

    private final VkPhysicalDevice physicalDevice;
    private final VkDevice device;

    private final VkPhysicalDeviceProperties deviceProperties;

    private final CommandPool transferCommandPool;
    private final CommandPool graphicsCommandPool;
    private final GPUQueue graphicsQueue;
    private final GPUQueue transferQueue;
    private final GPUQueue presentQueue;
    private final QueueFamilies queueFamilies;

    private final MemoryManager memoryManager;

    public GPUQueue getGraphicsQueue() {
        return this.graphicsQueue;
    }

    public GPUQueue getTransferQueue() {
        return this.transferQueue;
    }

    public GPUQueue getPresentQueue() {
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

    public MemoryManager getMemoryManager() {
        return this.memoryManager;
    }

    public CommandPool getTransferCommandPool() {
        return this.transferCommandPool;
    }

    public CommandPool getGraphicsCommandPool() {
        return this.graphicsCommandPool;
    }

    public VkPhysicalDeviceProperties getDeviceProperties() {
        return this.deviceProperties;
    }

    public DeviceContext(
            final VkPhysicalDevice physicalDevice,
            final QueueFamilies queueFamilies,
            final String[] extensionNames
    ) {
        LOG.debug("Creating device context");

        this.physicalDevice = physicalDevice;
        this.deviceProperties = VkPhysicalDeviceProperties.calloc();
        vkGetPhysicalDeviceProperties(this.physicalDevice, this.deviceProperties);

        this.queueFamilies = queueFamilies;

        try (final var stack = stackPush()) {
            final var pExtensions = stack.pointers(Arrays.stream(extensionNames)
                                                         .map(stack::UTF8)
                                                         .toArray(ByteBuffer[]::new));

            BufferUtil.forEachAsStringUTF8(pExtensions, name -> LOG.debug("-> Enabled device extension: {}", name));

            final var queueCreateInfos = createQueueCreateInfos(queueFamilies);

            final var deviceFeatures = VkPhysicalDeviceFeatures.callocStack();

            final var createInfo = VkDeviceCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                    .pQueueCreateInfos(queueCreateInfos)
                    .pEnabledFeatures(deviceFeatures)
                    .ppEnabledExtensionNames(pExtensions);

            final var pDevice = stack.mallocPointer(1);
            ensureSuccess(vkCreateDevice(this.physicalDevice, createInfo, null, pDevice),
                          "Creating logical device failed");

            this.device = new VkDevice(pDevice.get(0), physicalDevice, createInfo);
        }

        this.graphicsQueue = new GPUQueue(this.device, queueFamilies.graphics(), 0);
        this.transferQueue = new GPUQueue(this.device, queueFamilies.transfer(), 0);
        this.presentQueue = new GPUQueue(this.device, queueFamilies.present(), 0);

        this.memoryManager = new SliceMemoryManager(this);
        this.transferCommandPool = new CommandPool(this.device, queueFamilies.transfer());
        this.graphicsCommandPool = new CommandPool(this.device, queueFamilies.graphics());
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
        this.deviceProperties.free();

        this.memoryManager.close();
        this.transferCommandPool.close();
        this.graphicsCommandPool.close();
        vkDestroyDevice(this.device, null);
    }
}
