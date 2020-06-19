package fi.jakojaannos.riista.vulkan.internal.types;

import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_OPTIMAL;

/**
 * VkFormat - Specifies the tiling arrangement of data in an image.
 * <p>
 * Enum wrapper around {@link org.lwjgl.vulkan.VK11#VK_IMAGE_TILING_LINEAR VK_IMAGE_TILING_XXX} constants.
 */
public enum VkImageTiling {
    OPTIMAL(VK_IMAGE_TILING_OPTIMAL),
    LINEAR(VK_IMAGE_TILING_LINEAR);

    private final int tiling;

    VkImageTiling(final int tiling) {
        this.tiling = tiling;
    }

    public int asInt() {
        return this.tiling;
    }
}
