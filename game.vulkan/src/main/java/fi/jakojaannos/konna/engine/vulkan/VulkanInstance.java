package fi.jakojaannos.konna.engine.vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import fi.jakojaannos.konna.engine.util.BufferUtil;
import fi.jakojaannos.konna.engine.util.DebugMessenger;

import static fi.jakojaannos.konna.engine.util.VkUtil.ensureSuccess;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryStack.stackGet;
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
            final String[] layerNames,
            final String[] extensionNames
    ) {
        LOG.debug("Creating Vulkan instance");
        try (final var stack = stackPush()) {
            final var pLayerNames = selectLayers(layerNames);
            final var pExtensionNames = selectExtensions(extensionNames);

            if (checkLayerSupport(pLayerNames)) {
                throw new IllegalStateException("One or more requested validation layers are not available!");
            }
            if (checkExtensionSupport(pExtensionNames)) {
                throw new IllegalStateException("One or more requested instance extensions are not available!");
            }

            BufferUtil.forEachAsStringUTF8(pLayerNames, name -> LOG.debug("-> Enabled validation layer: {}", name));
            BufferUtil.forEachAsStringUTF8(pExtensionNames, name -> LOG.debug("-> Enabled instance extension: {}", name));

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
            ensureSuccess(vkCreateInstance(createInfo, null, pInstance),
                          "Vulkan instance creation failed");

            this.instance = new VkInstance(pInstance.get(0), createInfo);
            this.debugMessenger = new DebugMessenger(debugInfo, this.instance);
        }
    }

    private PointerBuffer selectLayers(final String[] layerNames) {
        final var stack = stackGet();
        return stack.pointers(Arrays.stream(layerNames)
                                    .map(stack::UTF8)
                                    .toArray(ByteBuffer[]::new));
    }

    private PointerBuffer selectExtensions(final String[] extensionNames) {
        final var stack = stackGet();
        final var pRequired = glfwGetRequiredInstanceExtensions();
        if (pRequired == null) {
            throw new IllegalStateException("GLFW could not figure out required extensions!");
        }

        final var pExtensionNames = stack.mallocPointer(pRequired.remaining() + extensionNames.length);
        pExtensionNames.put(pRequired);
        for (final var name : extensionNames) {
            pExtensionNames.put(stack.UTF8(name));
        }
        pExtensionNames.flip();

        return pExtensionNames;
    }

    private boolean checkLayerSupport(final PointerBuffer pRequiredLayerNames) {
        try (final var stack = stackPush()) {
            // Query count
            final var pCount = stack.mallocInt(1);
            ensureSuccess(vkEnumerateInstanceLayerProperties(pCount, null),
                          "Getting layer property count failed");

            // Query layer list
            final var pAvailableLayers = VkLayerProperties.calloc(pCount.get(0));
            ensureSuccess(vkEnumerateInstanceLayerProperties(pCount, pAvailableLayers),
                          "Querying layer properties failed");

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
            ensureSuccess(vkEnumerateInstanceExtensionProperties((ByteBuffer) null, pCount, null),
                          "Getting extension count failed");

            // Query layer list
            final var pAvailableExtensions = VkExtensionProperties.callocStack(pCount.get(0));
            ensureSuccess(vkEnumerateInstanceExtensionProperties((ByteBuffer) null, pCount, pAvailableExtensions),
                          "Querying extensions failed");

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
