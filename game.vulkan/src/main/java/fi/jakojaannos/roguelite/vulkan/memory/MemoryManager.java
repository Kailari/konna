package fi.jakojaannos.roguelite.vulkan.memory;

import org.lwjgl.vulkan.VkMemoryRequirements;

import fi.jakojaannos.roguelite.util.BitMask;
import fi.jakojaannos.roguelite.vulkan.types.VkMemoryPropertyFlags;

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
