package fi.jakojaannos.riista.vulkan.internal.types;

import fi.jakojaannos.riista.utilities.BitFlags;

import static org.lwjgl.vulkan.VK11.VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT;

public enum VkDescriptorPoolCreateFlags implements BitFlags {
    FREE_DESCRIPTOR_SET_BIT(VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT);

    private final int mask;

    @Override
    public int getMask() {
        return this.mask;
    }

    VkDescriptorPoolCreateFlags(final int mask) {
        this.mask = mask;
    }
}
