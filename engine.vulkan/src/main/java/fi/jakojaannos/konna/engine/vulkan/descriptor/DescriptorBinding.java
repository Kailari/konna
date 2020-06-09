package fi.jakojaannos.konna.engine.vulkan.descriptor;

public record DescriptorBinding(
        int slot,
        int descriptorType,
        int descriptorCount,
        int stageFlags
) {}
