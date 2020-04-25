package fi.jakojaannos.roguelite.vulkan.rendering;

import org.lwjgl.vulkan.VkExtent2D;

import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;

public class Framebuffers implements AutoCloseable {
    private final Framebuffer[] framebuffers;

    public Framebuffers(
            final DeviceContext deviceContext,
            final VkExtent2D swapchainExtent,
            final ImageView[] swapchainImageViews,
            final RenderPass renderPass
    ) {
        this.framebuffers = new Framebuffer[swapchainImageViews.length];

        for (int i = 0; i < this.framebuffers.length; i++) {
            this.framebuffers[i] = new Framebuffer(deviceContext,
                                                   swapchainExtent,
                                                   swapchainImageViews[i],
                                                   renderPass);
        }
    }

    public Framebuffer get(final int swapchainImageIndex) {
        return this.framebuffers[swapchainImageIndex];
    }

    @Override
    public void close() {
        for (final var handle : this.framebuffers) {
            handle.close();
        }
    }
}
