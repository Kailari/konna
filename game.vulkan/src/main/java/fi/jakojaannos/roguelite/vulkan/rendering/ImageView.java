package fi.jakojaannos.roguelite.vulkan.rendering;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class ImageView implements AutoCloseable {
    private final VkDevice device;
    private final long handle;

    public ImageView(final DeviceContext deviceContext, final long image, final int imageFormat) {
        this.device = deviceContext.getDevice();

        try (final var stack = stackPush()) {
            final var createInfo = VkImageViewCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                    .image(image)
                    .viewType(VK_IMAGE_VIEW_TYPE_2D)
                    .format(imageFormat);
            createInfo.components()
                      .r(VK_COMPONENT_SWIZZLE_IDENTITY)
                      .g(VK_COMPONENT_SWIZZLE_IDENTITY)
                      .b(VK_COMPONENT_SWIZZLE_IDENTITY)
                      .a(VK_COMPONENT_SWIZZLE_IDENTITY);
            createInfo.subresourceRange()
                      .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
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
