package fi.jakojaannos.riista.vulkan.util;

import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static fi.jakojaannos.riista.vulkan.util.VkUtil.translateVulkanResult;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.VK10.VK_FALSE;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public class DebugMessenger implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DebugMessenger.class);

    private static final int TYPE_FLAGS = VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT
                                          | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT
                                          | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;
    private static final int SEVERITY_FLAGS = VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT
                                              | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT
                                              | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT;

    private final VkInstance instance;
    private final long messenger;

    public DebugMessenger(
            final VkDebugUtilsMessengerCreateInfoEXT createInfo,
            final VkInstance instance
    ) {
        this.instance = instance;
        try (final var stack = stackPush()) {
            final var pMessenger = stack.mallocLong(1);
            final var result = vkCreateDebugUtilsMessengerEXT(instance, createInfo, null, pMessenger);
            if (result != VK_SUCCESS) {
                throw new IllegalStateException("Creating debug messenger failed: "
                                                + translateVulkanResult(result));
            }

            this.messenger = pMessenger.get(0);
        }
    }

    public static VkDebugUtilsMessengerCreateInfoEXT createInfo() {
        // NOTE: DO NOT STACK PUSH/POP (this is used elsewhere)
        return VkDebugUtilsMessengerCreateInfoEXT
                .callocStack()
                .sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
                .messageSeverity(SEVERITY_FLAGS)
                .messageType(TYPE_FLAGS)
                .pfnUserCallback(DebugMessenger::messageCallback);
    }

    @Override
    public void close() {
        vkDestroyDebugUtilsMessengerEXT(this.instance, this.messenger, null);
    }

    private static int messageCallback(
            final int severity,
            final int type,
            final long pCallbackData,
            final long pUserData
    ) {
        final var callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
        LOG.error(callbackData.pMessageString());
        return VK_FALSE;
    }
}
