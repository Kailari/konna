package fi.jakojaannos.riista.vulkan.internal.memory;

import org.lwjgl.vulkan.VkMemoryRequirements;

import fi.jakojaannos.riista.utilities.BitMask;
import fi.jakojaannos.riista.vulkan.internal.types.VkMemoryPropertyFlags;

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
