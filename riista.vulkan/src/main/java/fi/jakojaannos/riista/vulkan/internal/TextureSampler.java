package fi.jakojaannos.riista.vulkan.internal;

import org.lwjgl.vulkan.VkSamplerCreateInfo;

import fi.jakojaannos.riista.vulkan.internal.device.DeviceContext;
import fi.jakojaannos.riista.vulkan.internal.types.VkFilter;

import static fi.jakojaannos.riista.vulkan.util.VkUtil.ensureSuccess;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class TextureSampler implements AutoCloseable {
    private final DeviceContext deviceContext;

    private final long handle;

    public long getHandle() {
        return this.handle;
    }

    public TextureSampler(final DeviceContext deviceContext) {
        this(deviceContext, VkFilter.LINEAR, VkFilter.LINEAR);
    }

    public TextureSampler(
            final DeviceContext deviceContext,
            final VkFilter minFilter,
            final VkFilter magFilter
    ) {
        this.deviceContext = deviceContext;

        try (final var stack = stackPush()) {
            final var pSampler = stack.mallocLong(1);
            final var createInfo = VkSamplerCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
                    .magFilter(minFilter.safeAsInt(deviceContext))
                    .minFilter(magFilter.safeAsInt(deviceContext))
                    .addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                    .addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                    .addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                    .anisotropyEnable(true)
                    .maxAnisotropy(16)
                    .borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK)
                    .unnormalizedCoordinates(false)
                    .compareEnable(false)
                    .compareOp(VK_COMPARE_OP_ALWAYS)
                    .mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR)
                    .mipLodBias(0.0f)
                    .minLod(0.0f)
                    .maxLod(0.0f);

            ensureSuccess(vkCreateSampler(deviceContext.getDevice(),
                                          createInfo,
                                          null,
                                          pSampler),
                          "Creating texture sampler failed");
            this.handle = pSampler.get(0);
        }
    }

    @Override
    public void close() {
        vkDestroySampler(this.deviceContext.getDevice(), this.handle, null);

    }
}
