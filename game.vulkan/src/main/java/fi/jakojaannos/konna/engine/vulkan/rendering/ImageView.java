package fi.jakojaannos.konna.engine.vulkan.rendering;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import fi.jakojaannos.konna.engine.util.BitMask;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.engine.vulkan.GPUImage;
import fi.jakojaannos.konna.engine.vulkan.types.VkFormat;
import fi.jakojaannos.konna.engine.vulkan.types.VkImageAspectFlags;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class ImageView implements AutoCloseable {
    private final VkDevice device;
    private final long handle;

    public long getHandle() {
        return this.handle;
    }

    public ImageView(
            final DeviceContext deviceContext,
            final GPUImage image,
            final BitMask<VkImageAspectFlags> aspects
    ) {
        this(deviceContext, image.getHandle(), image.getFormat(), aspects);
    }

    public ImageView(
            final DeviceContext deviceContext,
            final long handle,
            final VkFormat format,
            final BitMask<VkImageAspectFlags> aspects
    ) {
        this.device = deviceContext.getDevice();

        try (final var stack = stackPush()) {
            final var createInfo = VkImageViewCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                    .image(handle)
                    .viewType(VK_IMAGE_VIEW_TYPE_2D)
                    .format(format.asInt());
            createInfo.components()
                      .r(VK_COMPONENT_SWIZZLE_IDENTITY)
                      .g(VK_COMPONENT_SWIZZLE_IDENTITY)
                      .b(VK_COMPONENT_SWIZZLE_IDENTITY)
                      .a(VK_COMPONENT_SWIZZLE_IDENTITY);
            createInfo.subresourceRange()
                      .aspectMask(aspects.mask())
                      .baseMipLevel(0)
                      .levelCount(1)
                      .baseArrayLayer(0)
                      .layerCount(1);

            final var pView = stack.mallocLong(1);
            vkCreateImageView(this.device, createInfo, null, pView);
            this.handle = pView.get(0);
        }
    }

    @Override
    public void close() {
        vkDestroyImageView(this.device, this.handle, null);
    }
}
