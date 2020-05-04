package fi.jakojaannos.roguelite.vulkan.memory;

import org.lwjgl.vulkan.VkMemoryRequirements;

public interface MemoryManager extends AutoCloseable {
    /**
     * Allocates memory matching the given memory requirements and properties.
     *
     * @param memoryRequirements description of the memory required
     * @param propertyFlags      desired memory properties
     *
     * @return handle to the allocated memory region
     */
    GPUMemory allocate(VkMemoryRequirements memoryRequirements, int propertyFlags);
}
