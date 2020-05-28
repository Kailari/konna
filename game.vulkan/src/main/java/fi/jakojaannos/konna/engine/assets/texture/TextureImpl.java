package fi.jakojaannos.konna.engine.assets.texture;

import java.nio.file.Path;

import fi.jakojaannos.konna.engine.assets.Texture;
import fi.jakojaannos.konna.engine.vulkan.GPUImage;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.engine.vulkan.rendering.ImageView;
import fi.jakojaannos.konna.engine.vulkan.types.VkImageAspectFlags;
import fi.jakojaannos.konna.engine.vulkan.types.VkImageTiling;
import fi.jakojaannos.konna.engine.vulkan.types.VkImageUsageFlags;
import fi.jakojaannos.konna.engine.vulkan.types.VkMemoryPropertyFlags;

import static fi.jakojaannos.konna.engine.util.BitMask.bitMask;

public class TextureImpl implements Texture {
    private final GPUImage image;
    private final ImageView imageView;

    public ImageView getImageView() {
        return this.imageView;
    }

    public TextureImpl(final DeviceContext deviceContext, final Path path) {
        this(deviceContext,
             new GPUImage(deviceContext,
                          path,
                          VkImageTiling.OPTIMAL,
                          bitMask(VkImageUsageFlags.SAMPLED_BIT),
                          bitMask(VkMemoryPropertyFlags.DEVICE_LOCAL_BIT)));
    }

    public TextureImpl(final DeviceContext deviceContext, final GPUImage image) {
        this.image = image;
        this.imageView = new ImageView(deviceContext,
                                       this.image,
                                       bitMask(VkImageAspectFlags.COLOR_BIT));
    }

    @Override
    public void close() {
        this.imageView.close();
        this.image.close();
    }
}
