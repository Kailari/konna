package fi.jakojaannos.roguelite.vulkan.memory.slice;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class SlicedGPUMemoryAllocation implements AutoCloseable {
    private final VkDevice device;

    private final long memoryHandle;
    private final long size;

    private final List<GPUMemorySlice> slices = new ArrayList<>();

    VkDevice getDevice() {
        return this.device;
    }

    /**
     * Gets the underlying native GPU memory handle/pointer. The handle can be used as the
     * <code>memory</code> parameter in (for example) {@link org.lwjgl.vulkan.VK10#vkBindBufferMemory(VkDevice,
     * long, long, long) vkBindBufferMemory} and {@link org.lwjgl.vulkan.VK10#vkBindImageMemory(VkDevice, long, long,
     * long) vkBindImageMemory}.
     *
     * @return the memory handle
     */
    long getHandle() {
        return this.memoryHandle;
    }

    public SlicedGPUMemoryAllocation(final VkDevice device, final int memoryType, final long size) {
        this.device = device;
        this.size = size;

        try (final var stack = stackPush()) {
            final var allocInfo = VkMemoryAllocateInfo
                    .callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                    .allocationSize(size)
                    .memoryTypeIndex(memoryType);

            final var pMemory = stack.mallocLong(1);
            ensureSuccess(vkAllocateMemory(this.device, allocInfo, null, pMemory),
                          "Could not allocate memory for a GPU buffer");

            this.memoryHandle = pMemory.get(0);
        }
    }

    void releaseSlice(final GPUMemorySlice memorySlice) {
        this.slices.remove(memorySlice);
    }

    public Optional<GPUMemorySlice> slice(final VkMemoryRequirements memoryRequirements) {
        final long sliceAlignment = memoryRequirements.alignment();
        final long sliceSize = memoryRequirements.size();

        // Fail immediately if this allocation is not large enough
        if (sliceSize > this.size) {
            return Optional.empty();
        }

        // Find first such index at which the next possible offset from slice's last index (next multiple of alignment
        // greater than the last index of the slice at sliceIndex) can fit the whole new slice.
        long offset = 0;
        var anyOverlaps = true;
        while (anyOverlaps) {
            // Find any overlaps and move the offset past them.
            // TODO: Use incrementing index instead of naive iteration, that could avoid sort, too
            anyOverlaps = false;
            for (final var slice : this.slices) {
                if (slice.overlaps(offset, offset + sliceSize)) {
                    final var lastIndex = slice.getOffset() + slice.getSize();
                    final var multiple = (long) (Math.ceil(lastIndex / (double) sliceAlignment));

                    offset = multiple * sliceAlignment;
                    anyOverlaps = true;
                }
            }

            // Make sure the slice still fits
            if (offset + sliceSize > this.size) {
                return Optional.empty();
            }
        }

        // The offset should now be at alignment which the slice can fit
        assert offset % sliceAlignment == 0;
        assert offset + sliceSize <= this.size;

        final var slice = new GPUMemorySlice(this, offset, sliceSize);
        this.slices.add(slice);
        this.slices.sort(Comparator.comparingLong(GPUMemorySlice::getOffset));
        return Optional.of(slice);
    }

    @Override
    public void close() {
        vkFreeMemory(this.device, this.memoryHandle, null);
    }
}
