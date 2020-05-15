package fi.jakojaannos.konna.vulkan.descriptor;

public record DescriptorBinding(
        int slot,
        int descriptorType,
        int descriptorCount,
        int stageFlags
) {}
