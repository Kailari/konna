package fi.jakojaannos.konna.engine.vulkan.descriptor;

import fi.jakojaannos.konna.engine.util.BitMask;
import fi.jakojaannos.konna.engine.vulkan.RenderingBackend;
import fi.jakojaannos.konna.engine.vulkan.rendering.Swapchain;
import fi.jakojaannos.konna.engine.vulkan.types.VkDescriptorPoolCreateFlags;

public class SwapchainImageDependentDescriptorPool extends DescriptorPool {
    private final Swapchain swapchain;

    private int oldSwapchainImageCount;

    @Override
    protected boolean isRecreateRequired() {
        return this.oldSwapchainImageCount != this.swapchain.getImageCount();
    }

    public SwapchainImageDependentDescriptorPool(
            final RenderingBackend backend,
            final int setsPerImage,
            final BitMask<VkDescriptorPoolCreateFlags> flags,
            final Pool... pools
    ) {
        super(backend.deviceContext(),
              () -> setsPerImage * backend.swapchain().getImageCount(),
              flags,
              pools);
        this.swapchain = backend.swapchain();
    }

    @Override
    protected void recreate() {
        super.recreate();
        this.oldSwapchainImageCount = this.swapchain.getImageCount();
    }
}
