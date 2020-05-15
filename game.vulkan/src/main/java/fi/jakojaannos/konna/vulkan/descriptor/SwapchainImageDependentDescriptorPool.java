package fi.jakojaannos.konna.vulkan.descriptor;

import fi.jakojaannos.konna.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.vulkan.rendering.Swapchain;

public class SwapchainImageDependentDescriptorPool extends DescriptorPool {
    private final Swapchain swapchain;

    private int oldSwapchainImageCount;

    @Override
    protected boolean isRecreateRequired() {
        return this.oldSwapchainImageCount != this.swapchain.getImageCount();
    }

    public SwapchainImageDependentDescriptorPool(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final int setsPerImage,
            final Pool... pools
    ) {
        super(deviceContext, () -> setsPerImage * swapchain.getImageCount(), pools);
        this.swapchain = swapchain;
    }

    @Override
    protected void recreate() {
        super.recreate();
        this.oldSwapchainImageCount = this.swapchain.getImageCount();
    }
}
