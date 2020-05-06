package fi.jakojaannos.roguelite.vulkan;

import org.lwjgl.vulkan.VkSamplerCreateInfo;

import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.ImageView;
import fi.jakojaannos.roguelite.vulkan.textures.GPUImage;

import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class Texture implements AutoCloseable {
    private final DeviceContext deviceContext;

    private final ImageView imageView;

    // FIXME: Move the sampler out of here, it can very well be re-used for multiple textures
    private final long samplerHandle;

    public long getImageViewHandle() {
        return this.imageView.getHandle();
    }

    public long getSamplerHandle() {
        return this.samplerHandle;
    }

    public Texture(
            final DeviceContext deviceContext,
            final GPUImage image
    ) {
        this.deviceContext = deviceContext;

        this.imageView = new ImageView(deviceContext,
                                       image.getHandle(),
                                       image.getFormat());

        try (final var stack = stackPush()) {
            final var pSampler = stack.mallocLong(1);
            final var createInfo = VkSamplerCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
                    .magFilter(VK_FILTER_LINEAR)
                    .minFilter(VK_FILTER_LINEAR)
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
            this.samplerHandle = pSampler.get(0);
        }
    }

    @Override
    public void close() {
        vkDestroySampler(this.deviceContext.getDevice(), this.samplerHandle, null);
        this.imageView.close();
    }
}
