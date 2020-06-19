package fi.jakojaannos.riista.vulkan.internal.descriptor;

import fi.jakojaannos.riista.utilities.BitMask;
import fi.jakojaannos.riista.vulkan.internal.RenderingBackend;
import fi.jakojaannos.riista.vulkan.rendering.Swapchain;
import fi.jakojaannos.riista.vulkan.internal.types.VkDescriptorPoolCreateFlags;

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
