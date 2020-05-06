package fi.jakojaannos.roguelite.vulkan.descriptor;

public record DescriptorBinding(
        int slot,
        int descriptorType,
        int descriptorCount,
        int stageFlags
) {}
