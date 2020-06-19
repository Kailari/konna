package fi.jakojaannos.riista.vulkan.rendering;

import org.lwjgl.vulkan.VkClearValue;

import fi.jakojaannos.riista.vulkan.internal.types.VkFormat;
import fi.jakojaannos.riista.vulkan.internal.types.VkSampleCountFlags;

public interface Attachment {
    boolean isRecreateRequired();

    VkFormat format();

    VkSampleCountFlags samples();

    int loadOp();

    int storeOp();

    int stencilLoadOp();

    int stencilStoreOp();

    int initialLayout();

    int finalLayout();

    default void onRecreate() {}

    void clearValue(VkClearValue outValue);
}
