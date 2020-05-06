package fi.jakojaannos.roguelite.vulkan.descriptor;

public record DescriptorSetBinding(
        int slot,
        int descriptorType,
        int descriptorCount,
        int stageFlags
) {}
