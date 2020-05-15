package fi.jakojaannos.konna.vulkan.memory;

import org.lwjgl.vulkan.VkMemoryRequirements;

import fi.jakojaannos.konna.util.BitMask;
import fi.jakojaannos.konna.vulkan.types.VkMemoryPropertyFlags;

public interface MemoryManager extends AutoCloseable {
    /**
     * Allocates memory matching the given memory requirements and properties.
     *
     * @param memoryRequirements description of the memory required
     * @param properties         desired memory properties
     *
     * @return handle to the allocated memory region
     */
    GPUMemory allocate(VkMemoryRequirements memoryRequirements, BitMask<VkMemoryPropertyFlags> properties);

    @Override
    void close();
}
