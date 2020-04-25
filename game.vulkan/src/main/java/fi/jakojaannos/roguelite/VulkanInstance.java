package fi.jakojaannos.roguelite;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import fi.jakojaannos.roguelite.util.BufferUtil;
import fi.jakojaannos.roguelite.util.DebugMessenger;

import static fi.jakojaannos.roguelite.util.VkUtil.translateVulkanResult;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK11.VK_API_VERSION_1_1;

public class VulkanInstance implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(VulkanInstance.class);

    private final VkInstance instance;
    private final DebugMessenger debugMessenger;

    public VkInstance getHandle() {
        return this.instance;
    }

    public VulkanInstance(
            final PointerBuffer pLayerNames,
            final PointerBuffer pExtensionNames
    ) {
        try (final var stack = stackPush()) {
            BufferUtil.forEachAsStringUTF8(pLayerNames,
                                           name -> LOG.info("-> Validation layer: {}", name));
            BufferUtil.forEachAsStringUTF8(pExtensionNames,
                                           name -> LOG.info("-> Instance extension: {}", name));

            if (checkLayerSupport(pLayerNames)) {
                throw new IllegalStateException("One or more requested validation layers are not available!");
            }
            if (checkExtensionSupport(pExtensionNames)) {
                throw new IllegalStateException("One or more requested instance extensions are not available!");
            }

            final var appInfo = VkApplicationInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                    .apiVersion(VK_API_VERSION_1_1)
                    .pApplicationName(stack.UTF8("Konna"))
                    .applicationVersion(VK_MAKE_VERSION(1, 0, 0))
                    .pEngineName(stack.UTF8("Riista [Vulkan]"))
                    .engineVersion(VK_MAKE_VERSION(1, 0, 0));

            final var debugInfo = DebugMessenger.createInfo();
            final var createInfo = VkInstanceCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                    .pApplicationInfo(appInfo)
                    .ppEnabledExtensionNames(pExtensionNames)
                    .ppEnabledLayerNames(pLayerNames)
                    .pNext(debugInfo.address());


            final var pInstance = stack.mallocPointer(1);
            final var result = vkCreateInstance(createInfo, null, pInstance);
            if (result != VK_SUCCESS) {
                throw new IllegalStateException("Oh no! VkInstance creation failed! "
                                                + translateVulkanResult(result));
            }

            this.instance = new VkInstance(pInstance.get(0), createInfo);
            this.debugMessenger = new DebugMessenger(debugInfo, this.instance);
        }
    }

    private boolean checkLayerSupport(final PointerBuffer pRequiredLayerNames) {
        try (final var stack = stackPush()) {
            // Query count
            final var pCount = stack.mallocInt(1);
            final var countResult = vkEnumerateInstanceLayerProperties(pCount, null);
            if (countResult != VK_SUCCESS) {
                throw new IllegalStateException("Getting layer property count failed: "
                                                + translateVulkanResult(countResult));
            }

            // Query layer list
            final var pAvailableLayers = VkLayerProperties.calloc(pCount.get(0));
            final var queryResult = vkEnumerateInstanceLayerProperties(pCount, pAvailableLayers);
            if (queryResult != VK_SUCCESS) {
                throw new IllegalStateException("Querying layer properties failed: "
                                                + translateVulkanResult(countResult));
            }

            return BufferUtil.filteredForEachAsStringUTF8(
                    pRequiredLayerNames,
                    name -> pAvailableLayers.stream()
                                            .map(VkLayerProperties::layerNameString)
                                            .noneMatch(name::equals),
                    notFound -> LOG.error("Validation layer \"{}\" not found.", notFound));
        }
    }

    private boolean checkExtensionSupport(final PointerBuffer pRequiredExtensions) {
        try (final var stack = stackPush()) {
            // Query count
            final var pCount = stack.mallocInt(1);
            final var countResult = vkEnumerateInstanceExtensionProperties((ByteBuffer) null, pCount, null);
            if (countResult != VK_SUCCESS) {
                throw new IllegalStateException("Getting extension count failed: "
                                                + translateVulkanResult(countResult));
            }

            // Query layer list
            final var pAvailableExtensions = VkExtensionProperties.calloc(pCount.get(0));
            final var queryResult = vkEnumerateInstanceExtensionProperties((ByteBuffer) null, pCount, pAvailableExtensions);
            if (queryResult != VK_SUCCESS) {
                throw new IllegalStateException("Querying extensions failed: "
                                                + translateVulkanResult(countResult));
            }

            return BufferUtil.filteredForEachAsStringUTF8(
                    pRequiredExtensions,
                    name -> pAvailableExtensions.stream()
                                                .map(VkExtensionProperties::extensionNameString)
                                                .noneMatch(name::equals),
                    notFound -> LOG.error("Extension layer \"{}\" not found.", notFound));
        }
    }

    @Override
    public void close() {
        this.debugMessenger.close();
        vkDestroyInstance(this.instance, null);
    }
}
