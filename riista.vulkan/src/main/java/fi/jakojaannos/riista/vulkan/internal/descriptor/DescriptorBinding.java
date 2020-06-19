package fi.jakojaannos.riista.vulkan.internal.descriptor;

public record DescriptorBinding(
        int slot,
        int descriptorType,
        int descriptorCount,
        int stageFlags
) {}
