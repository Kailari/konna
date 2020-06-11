package fi.jakojaannos.riista.vulkan.rendering;

import org.lwjgl.vulkan.VkClearValue;

import fi.jakojaannos.riista.vulkan.internal.types.VkFormat;
import fi.jakojaannos.riista.vulkan.internal.types.VkSampleCountFlags;

import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class PresentColorAttachment implements Attachment {
    private final Swapchain swapchain;

    private VkFormat oldSwapchainImageFormat;

    @Override
    public boolean isRecreateRequired() {
        return this.oldSwapchainImageFormat != this.swapchain.getImageFormat();
    }

    public PresentColorAttachment(final Swapchain swapchain) {
        this.swapchain = swapchain;
    }

    @Override
    public void onRecreate() {
        this.oldSwapchainImageFormat = this.swapchain.getImageFormat();
    }

    @Override
    public VkFormat format() {
        return this.swapchain.getImageFormat();
    }

    @Override
    public VkSampleCountFlags samples() {
        return VkSampleCountFlags.COUNT_1;
    }

    @Override
    public int loadOp() {
        return VK_ATTACHMENT_LOAD_OP_CLEAR;
    }

    @Override
    public int storeOp() {
        return VK_ATTACHMENT_STORE_OP_STORE;
    }

    @Override
    public int stencilLoadOp() {
        return VK_ATTACHMENT_LOAD_OP_DONT_CARE;
    }

    @Override
    public int stencilStoreOp() {
        return VK_ATTACHMENT_STORE_OP_DONT_CARE;
    }

    @Override
    public int initialLayout() {
        return VK_IMAGE_LAYOUT_UNDEFINED;
    }

    @Override
    public int finalLayout() {
        return VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
    }

    @Override
    public void clearValue(final VkClearValue outValue) {
        outValue.color()
                .float32(0, 0.0f)
                .float32(1, 0.0f)
                .float32(2, 0.0f)
                .float32(3, 1.0f);
    }
}
