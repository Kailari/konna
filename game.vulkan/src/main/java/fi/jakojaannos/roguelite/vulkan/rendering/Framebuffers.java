package fi.jakojaannos.roguelite.vulkan.rendering;

import org.lwjgl.vulkan.VkDevice;

import fi.jakojaannos.roguelite.util.RecreateCloseable;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;

public class Framebuffers extends RecreateCloseable {
    private final VkDevice device;
    private final Swapchain swapchain;
    private final RenderPass renderPass;

    private Framebuffer[] framebuffers;

    @Override
    protected boolean isRecreateRequired() {
        return true;
    }

    public Framebuffers(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final RenderPass renderPass
    ) {
        this.device = deviceContext.getDevice();
        this.swapchain = swapchain;
        this.renderPass = renderPass;

        tryRecreate();
    }

    @Override
    protected void recreate() {
        final var imageViews = this.swapchain.getImageViews();
        this.framebuffers = new Framebuffer[imageViews.length];

        for (int i = 0; i < this.framebuffers.length; i++) {
            this.framebuffers[i] = new Framebuffer(this.device,
                                                   this.swapchain.getExtent(),
                                                   imageViews[i],
                                                   this.renderPass);
        }
    }

    @Override
    protected void cleanup() {
        for (final var handle : this.framebuffers) {
            handle.close();
        }
    }

    public Framebuffer get(final int swapchainImageIndex) {
        return this.framebuffers[swapchainImageIndex];
    }
}
