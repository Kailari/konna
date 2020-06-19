package fi.jakojaannos.riista.vulkan.internal.types;

import fi.jakojaannos.riista.utilities.BitFlags;

import static org.lwjgl.vulkan.VK10.*;

public enum VkImageUsageFlags implements BitFlags {
    TRANSFER_SRC_BIT(VK_IMAGE_USAGE_TRANSFER_SRC_BIT),
    TRANSFER_DST_BIT(VK_IMAGE_USAGE_TRANSFER_DST_BIT),
    SAMPLED_BIT(VK_IMAGE_USAGE_SAMPLED_BIT),
    STORAGE_BIT(VK_IMAGE_USAGE_STORAGE_BIT),
    COLOR_ATTACHMENT_BIT(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT),
    DEPTH_STENCIL_ATTACHMENT_BIT(VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT),
    TRANSIENT_ATTACHMENT_BIT(VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT),
    INPUT_ATTACHMENT_BIT(VK_IMAGE_USAGE_INPUT_ATTACHMENT_BIT);

    private final int mask;

    @Override
    public int getMask() {
        return this.mask;
    }

    VkImageUsageFlags(final int mask) {
        this.mask = mask;
    }
}
