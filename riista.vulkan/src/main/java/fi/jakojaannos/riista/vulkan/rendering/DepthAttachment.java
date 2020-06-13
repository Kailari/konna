package fi.jakojaannos.riista.vulkan.rendering;

import org.lwjgl.vulkan.VkClearValue;

import fi.jakojaannos.riista.vulkan.internal.device.DeviceContext;
import fi.jakojaannos.riista.vulkan.internal.types.VkFormat;
import fi.jakojaannos.riista.vulkan.internal.types.VkSampleCountFlags;
import fi.jakojaannos.riista.vulkan.rendering.Attachment;

import static org.lwjgl.vulkan.VK10.*;

public class DepthAttachment implements Attachment {
    private final DeviceContext deviceContext;

    @Override
    public boolean isRecreateRequired() {
        return false;
    }

    public DepthAttachment(final DeviceContext deviceContext) {
        this.deviceContext = deviceContext;
    }

    @Override
    public VkFormat format() {
        return VkFormat.findDepthFormat(this.deviceContext);
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
        return VK_ATTACHMENT_STORE_OP_DONT_CARE;
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
        return VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
    }

    @Override
    public void clearValue(final VkClearValue outValue) {
        outValue.depthStencil()
                .set(1.0f, 0);
    }
}
