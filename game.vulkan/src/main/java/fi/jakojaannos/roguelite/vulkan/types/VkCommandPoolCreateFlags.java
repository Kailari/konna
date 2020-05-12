package fi.jakojaannos.roguelite.vulkan.types;

import fi.jakojaannos.roguelite.util.BitFlags;

import static org.lwjgl.vulkan.VK10.*;

public enum VkCommandPoolCreateFlags implements BitFlags {
    TRANSIENT_BIT(VK_COMMAND_POOL_CREATE_TRANSIENT_BIT),
    RESET_COMMAND_BUFFER_BIT(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);

    private final int mask;

    VkCommandPoolCreateFlags(final int mask) {
        this.mask = mask;
    }

    @Override
    public int getMask() {
        return this.mask;
    }
}
