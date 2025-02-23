package fi.jakojaannos.riista.vulkan.internal.descriptor;

import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;

import java.util.Arrays;
import java.util.function.IntSupplier;

import fi.jakojaannos.riista.utilities.BitMask;
import fi.jakojaannos.riista.vulkan.util.RecreateCloseable;
import fi.jakojaannos.riista.vulkan.internal.device.DeviceContext;
import fi.jakojaannos.riista.vulkan.internal.types.VkDescriptorPoolCreateFlags;

import static fi.jakojaannos.riista.vulkan.util.VkUtil.ensureSuccess;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class DescriptorPool extends RecreateCloseable {
    private final DeviceContext deviceContext;

    private final IntSupplier maxSetsSupplier;
    private final Pool[] pools;
    private final int createFlags;

    private long handle;

    public long getHandle() {
        return this.handle;
    }

    public DescriptorPool(
            final DeviceContext deviceContext,
            final IntSupplier maxSetsSupplier,
            final BitMask<VkDescriptorPoolCreateFlags> flags,
            final Pool... pools
    ) {
        this.deviceContext = deviceContext;
        this.createFlags = flags.mask();

        this.maxSetsSupplier = maxSetsSupplier;
        this.pools = pools;
    }

    @Override
    protected void recreate() {
        try (final var stack = stackPush()) {
            final var poolSizes = VkDescriptorPoolSize.callocStack(this.pools.length);
            for (int i = 0; i < this.pools.length; i++) {
                poolSizes.get(i)
                         .type(this.pools[i].type)
                         .descriptorCount(this.pools[i].descriptorCountSupplier.getAsInt());
            }

            final var createInfo = VkDescriptorPoolCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
                    .pPoolSizes(poolSizes)
                    .maxSets(this.maxSetsSupplier.getAsInt())
                    .flags(this.createFlags);

            final var pPool = stack.mallocLong(1);
            ensureSuccess(vkCreateDescriptorPool(this.deviceContext.getDevice(),
                                                 createInfo,
                                                 null,
                                                 pPool),
                          "Creating descriptor pool failed");
            this.handle = pPool.get(0);
        }
    }

    @Override
    public void cleanup() {
        vkDestroyDescriptorPool(this.deviceContext.getDevice(), this.handle, null);
    }

    public long[] allocate(final DescriptorSetLayout layout, final int count) {
        final var handles = new long[count];

        try (final var stack = stackPush()) {
            final var layouts = new long[count];
            Arrays.fill(layouts, layout.getHandle());

            final var allocateInfo = VkDescriptorSetAllocateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                    .descriptorPool(this.handle)
                    .pSetLayouts(stack.longs(layouts));

            ensureSuccess(vkAllocateDescriptorSets(this.deviceContext.getDevice(),
                                                   allocateInfo,
                                                   handles),
                          "Allocating descriptor sets failed");
        }

        return handles;
    }

    public void free(final long... descriptorSets) {
        ensureSuccess(vkFreeDescriptorSets(this.deviceContext.getDevice(), this.handle, descriptorSets),
                      "Freeing descriptor sets failed");
    }

    public record Pool(int type, IntSupplier descriptorCountSupplier) {}
}
