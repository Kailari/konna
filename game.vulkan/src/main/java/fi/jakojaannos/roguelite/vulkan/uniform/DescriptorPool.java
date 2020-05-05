package fi.jakojaannos.roguelite.vulkan.uniform;

import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;

import fi.jakojaannos.roguelite.util.RecreateCloseable;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;

import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class DescriptorPool extends RecreateCloseable {
    private final DeviceContext deviceContext;

    private final int maxSets;
    private final Pool[] pools;

    private long handle;

    public long getHandle() {
        return this.handle;
    }

    @Override
    protected boolean isRecreateRequired() {
        return true;
    }

    public DescriptorPool(
            final DeviceContext deviceContext,
            final int maxSets,
            final Pool... pools
    ) {
        this.deviceContext = deviceContext;

        this.maxSets = maxSets;
        this.pools = pools;

        tryRecreate();
    }

    @Override
    protected void recreate() {
        try (final var stack = stackPush()) {
            final var poolSizes = VkDescriptorPoolSize.callocStack(this.pools.length);
            for (int i = 0; i < this.pools.length; i++) {
                poolSizes.get(i)
                         .type(this.pools[i].type)
                         .descriptorCount(this.pools[i].descriptorCount);
            }

            final var createInfo = VkDescriptorPoolCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
                    .pPoolSizes(poolSizes)
                    .maxSets(this.maxSets);

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

    public record Pool(int type, int descriptorCount) {}
}
