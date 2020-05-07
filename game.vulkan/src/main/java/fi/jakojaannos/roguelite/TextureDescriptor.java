package fi.jakojaannos.roguelite;

import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import fi.jakojaannos.roguelite.util.RecreateCloseable;
import fi.jakojaannos.roguelite.vulkan.TextureSampler;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorBinding;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.ImageView;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

// FIXME: The layout should not be here. It is very likely we want to bind different texture descriptor
//        sets with the same layout. e.g. mesh rendering is the very same for multiple meshes, but
//        texture/material parameters change on-the-fly. That requires binding different set to the
//        "same layout". (?)
public class TextureDescriptor extends RecreateCloseable {
    private final DeviceContext deviceContext;
    private final Swapchain swapchain;

    private final DescriptorSetLayout layout;
    private final TextureSampler sampler;
    private final DescriptorPool descriptorPool;

    private final ImageView imageView;

    private long[] descriptorSets;

    @Override
    protected boolean isRecreateRequired() {
        return true;
    }

    public DescriptorSetLayout getLayout() {
        return this.layout;
    }

    public TextureDescriptor(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final ImageView imageView,
            final TextureSampler sampler
    ) {
        this.deviceContext = deviceContext;
        this.swapchain = swapchain;
        this.descriptorPool = descriptorPool;
        this.imageView = imageView;

        this.sampler = sampler;

        this.layout = new DescriptorSetLayout(deviceContext,
                                              new DescriptorBinding(0,
                                                                    VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                                                                    1,
                                                                    VK_SHADER_STAGE_FRAGMENT_BIT));
    }

    public long get(final int imageIndex) {
        return this.descriptorSets[imageIndex];
    }

    @Override
    protected void recreate() {
        this.descriptorSets = this.descriptorPool.allocate(this.layout, this.swapchain.getImageCount());

        for (final var descriptorSet : this.descriptorSets) {
            try (final var ignored = stackPush()) {
                final var imageInfo = VkDescriptorImageInfo
                        .callocStack(1)
                        .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                        .imageView(this.imageView.getHandle())
                        .sampler(this.sampler.getHandle());

                final var descriptorWrites = VkWriteDescriptorSet
                        .callocStack(1)
                        .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                        .dstSet(descriptorSet)
                        .dstBinding(0)
                        .dstArrayElement(0)
                        .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                        .descriptorCount(1)
                        .pImageInfo(imageInfo);

                vkUpdateDescriptorSets(this.deviceContext.getDevice(), descriptorWrites, null);
            }
        }
    }

    @Override
    protected void cleanup() {
    }

    @Override
    public void close() {
        super.close();
        this.layout.close();
    }
}
