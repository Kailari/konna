package fi.jakojaannos.roguelite.vulkan;

import org.lwjgl.vulkan.VkExtent2D;

import fi.jakojaannos.roguelite.util.RecreateCloseable;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.ImageView;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;
import fi.jakojaannos.roguelite.vulkan.types.*;

import static fi.jakojaannos.roguelite.util.BitMask.bitMask;

public class DepthTexture extends RecreateCloseable {
    private final DeviceContext deviceContext;
    private final Swapchain swapchain;
    private final VkExtent2D oldExtent;
    private GPUImage image;
    private ImageView imageView;

    public ImageView getImageView() {
        return this.imageView;
    }

    @Override
    protected boolean isRecreateRequired() {
        return this.oldExtent.width() != this.swapchain.getExtent().width()
               || this.oldExtent.height() != this.swapchain.getExtent().height();
    }

    public DepthTexture(
            final DeviceContext deviceContext,
            final Swapchain swapchain
    ) {
        this.deviceContext = deviceContext;
        this.swapchain = swapchain;
        this.oldExtent = VkExtent2D.calloc();
    }

    @Override
    protected void recreate() {
        this.oldExtent.set(this.swapchain.getExtent());

        this.image = new GPUImage(this.deviceContext,
                                  this.swapchain.getExtent().width(),
                                  this.swapchain.getExtent().height(),
                                  VkFormat.findDepthFormat(this.deviceContext),
                                  VkImageTiling.OPTIMAL,
                                  bitMask(VkImageUsageFlags.DEPTH_STENCIL_ATTACHMENT_BIT),
                                  bitMask(VkMemoryPropertyFlags.DEVICE_LOCAL_BIT));
        this.imageView = new ImageView(this.deviceContext,
                                       this.image,
                                       bitMask(VkImageAspectFlags.DEPTH_BIT));
    }

    @Override
    protected void cleanup() {
        this.imageView.close();
        this.image.close();
    }

    @Override
    public void close() {
        super.close();
        this.oldExtent.free();
    }
}
