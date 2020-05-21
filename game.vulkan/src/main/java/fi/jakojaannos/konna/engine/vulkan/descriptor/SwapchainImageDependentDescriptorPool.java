package fi.jakojaannos.konna.engine.vulkan.descriptor;

import fi.jakojaannos.konna.engine.util.BitMask;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.engine.vulkan.rendering.Swapchain;
import fi.jakojaannos.konna.engine.vulkan.types.VkDescriptorPoolCreateFlags;

import static fi.jakojaannos.konna.engine.util.BitMask.bitMask;

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
            final BitMask<VkDescriptorPoolCreateFlags> flags,
            final Pool... pools
    ) {
        super(deviceContext,
              () -> setsPerImage * swapchain.getImageCount(),
              flags,
              pools);
        this.swapchain = swapchain;
    }

    @Override
    protected void recreate() {
        super.recreate();
        this.oldSwapchainImageCount = this.swapchain.getImageCount();
    }
}
