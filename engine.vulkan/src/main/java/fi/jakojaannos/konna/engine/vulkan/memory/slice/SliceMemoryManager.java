package fi.jakojaannos.konna.engine.vulkan.memory.slice;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import fi.jakojaannos.riista.utilities.BitMask;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.engine.vulkan.memory.MemoryManager;
import fi.jakojaannos.konna.engine.vulkan.types.VkMemoryPropertyFlags;

import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;

public class SliceMemoryManager implements MemoryManager {
    private static final Logger LOG = LoggerFactory.getLogger(SliceMemoryManager.class);

    /**
     * Any memory heap with size less than this threshold is treated as a "small" heap. Small heaps are allocated in
     * smaller chunks, when possible.
     */
    private static final int SMALL_HEAP_SIZE_THRESHOLD = 1_073_741_824; // 1 GiB
    private static final int ALLOCATIONS_PER_SMALL_HEAP = 8;

    /**
     * Default size of an allocation on any "large/normal" heap.
     */
    private static final int DEFAULT_ALLOCATION_SIZE = 268_435_456; // 256 MiB

    private final int maxAllocations;
    private final long defaultAllocationSize;

    private final VkPhysicalDeviceMemoryProperties memoryProperties;
    private final VkDevice device;

    private final Map<Integer, List<SlicedGPUMemoryAllocation>> allocations = new HashMap<>();

    private int allocationCount;


    public SliceMemoryManager(final DeviceContext deviceContext) {
        this(deviceContext, DEFAULT_ALLOCATION_SIZE);
    }

    public SliceMemoryManager(
            final DeviceContext deviceContext,
            final long defaultAllocationSize
    ) {
        this.device = deviceContext.getDevice();
        this.defaultAllocationSize = defaultAllocationSize;

        this.maxAllocations = deviceContext.getDeviceProperties()
                                           .limits()
                                           .maxMemoryAllocationCount();

        this.memoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
        vkGetPhysicalDeviceMemoryProperties(deviceContext.getPhysicalDevice(), this.memoryProperties);
    }

    @Override
    public GPUMemorySlice allocate(
            final VkMemoryRequirements memoryRequirements,
            final BitMask<VkMemoryPropertyFlags> properties
    ) {
        final var memoryType = findMemoryType(memoryRequirements.memoryTypeBits(), properties);
        return findSuitableAllocation(memoryRequirements, memoryType)
                .orElseGet(() -> allocateAndSlice(memoryRequirements, memoryType));
    }

    /**
     * Tries to fit the described memory requirements to existing allocations and creates and returns slice to the first
     * available allocation.
     *
     * @param memoryRequirements description of the memory required
     * @param memoryType         type of the allocated memory
     *
     * @return memory slice from some existing allocation. Empty if no suitable allocation is found.
     */
    private Optional<GPUMemorySlice> findSuitableAllocation(
            final VkMemoryRequirements memoryRequirements,
            final int memoryType
    ) {
        final var withMatchingType = this.allocations.computeIfAbsent(memoryType, key -> new ArrayList<>());
        for (final var allocation : withMatchingType) {
            final var maybeSlice = allocation.slice(memoryRequirements);
            if (maybeSlice.isPresent()) {
                return maybeSlice;
            }
        }

        return Optional.empty();
    }

    /**
     * Creates a new allocation and slices it for the given memory requirements.
     *
     * @param memoryRequirements description of the memory required
     * @param memoryType         type of the allocated memory
     *
     * @return memory slice from the new allocation
     */
    private GPUMemorySlice allocateAndSlice(final VkMemoryRequirements memoryRequirements, final int memoryType) {
        if (this.allocationCount > this.maxAllocations) {
            LOG.warn("Allocating over the specification allocation limit! Number of allocations: {}",
                     this.allocationCount);
        }

        final var size = getAllocationSize(memoryRequirements, memoryType);
        final var allocation = new SlicedGPUMemoryAllocation(this.device, memoryType, size);
        this.allocations.computeIfAbsent(memoryType, key -> new ArrayList<>())
                        .add(allocation);
        ++this.allocationCount;

        // SAFETY: This should never throw as long as allocation is at least `memoryRequirements.size()`
        return allocation.slice(memoryRequirements).orElseThrow();
    }

    private int findMemoryType(final int typeFilter, final BitMask<VkMemoryPropertyFlags> properties) {
        for (var memoryType = 0; memoryType < this.memoryProperties.memoryTypeCount(); ++memoryType) {
            final var typeIsSuitable = (typeFilter & (1 << memoryType)) != 0;
            final var hasAllProperties = properties.matches(this.memoryProperties.memoryTypes(memoryType)
                                                                                 .propertyFlags());

            if (typeIsSuitable && hasAllProperties) {
                return memoryType;
            }
        }

        throw new IllegalStateException("Could not find suitable memory type!");
    }

    /**
     * Figures out size for the allocation. Takes in account the set default size and the heap size of the given memory
     * type.
     *
     * @param memoryRequirements description of the required memory
     * @param memoryType         type of the allocated memory
     *
     * @return the size for the new allocation
     */
    private long getAllocationSize(final VkMemoryRequirements memoryRequirements, final int memoryType) {
        final var heapIndex = this.memoryProperties.memoryTypes(memoryType)
                                                   .heapIndex();
        final var heap = this.memoryProperties.memoryHeaps(heapIndex);

        // Small heap (e.g. less than 1 GiB)
        if (heap.size() < SMALL_HEAP_SIZE_THRESHOLD) {
            return Math.max(heap.size() / ALLOCATIONS_PER_SMALL_HEAP, memoryRequirements.size());
        }

        // Large heap, allocate using the default size
        return Math.max(this.defaultAllocationSize, memoryRequirements.size());
    }

    @Override
    public void close() {
        for (final var allocations : this.allocations.values()) {
            allocations.forEach(SlicedGPUMemoryAllocation::close);
        }
        this.memoryProperties.free();
    }
}
