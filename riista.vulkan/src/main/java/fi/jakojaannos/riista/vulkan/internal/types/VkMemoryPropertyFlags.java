package fi.jakojaannos.riista.vulkan.internal.types;

import fi.jakojaannos.riista.utilities.BitFlags;

import static org.lwjgl.vulkan.VK10.*;

public enum VkMemoryPropertyFlags implements BitFlags {
    DEVICE_LOCAL_BIT(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT),
    HOST_VISIBLE_BIT(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT),
    HOST_COHERENT_BIT(VK_MEMORY_PROPERTY_HOST_COHERENT_BIT),
    HOST_CACHED_BIT(VK_MEMORY_PROPERTY_HOST_CACHED_BIT),
    LAZILY_ALLOCATED_BIT(VK_MEMORY_PROPERTY_LAZILY_ALLOCATED_BIT);

    private final int mask;

    @Override
    public int getMask() {
        return this.mask;
    }

    VkMemoryPropertyFlags(final int mask) {
        this.mask = mask;
    }
}
