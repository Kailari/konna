package fi.jakojaannos.riista.vulkan.internal.types;

import fi.jakojaannos.riista.utilities.BitFlags;

import static org.lwjgl.vulkan.VK10.*;

public enum VkSampleCountFlags implements BitFlags {
    COUNT_1(VK_SAMPLE_COUNT_1_BIT),
    COUNT_2(VK_SAMPLE_COUNT_2_BIT),
    COUNT_4(VK_SAMPLE_COUNT_4_BIT),
    COUNT_8(VK_SAMPLE_COUNT_8_BIT),
    COUNT_16(VK_SAMPLE_COUNT_16_BIT),
    COUNT_32(VK_SAMPLE_COUNT_32_BIT),
    COUNT_64(VK_SAMPLE_COUNT_64_BIT);

    private final int mask;

    @Override
    public int getMask() {
        return this.mask;
    }

    VkSampleCountFlags(final int mask) {
        this.mask = mask;
    }

    public int asInt() {
        return this.mask;
    }
}
