package fi.jakojaannos.roguelite.vulkan.types;

import fi.jakojaannos.roguelite.util.BitFlags;

import static org.lwjgl.vulkan.VK10.*;

public enum VkImageAspectFlags implements BitFlags {
    COLOR_BIT(VK_IMAGE_ASPECT_COLOR_BIT),
    DEPTH_BIT(VK_IMAGE_ASPECT_DEPTH_BIT),
    STENCIL_BIT(VK_IMAGE_ASPECT_STENCIL_BIT),
    METADATA_BIT(VK_IMAGE_ASPECT_METADATA_BIT);

    private final int mask;

    @Override
    public int getMask() {
        return this.mask;
    }

    VkImageAspectFlags(final int mask) {
        this.mask = mask;
    }
}
