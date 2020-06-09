package fi.jakojaannos.riista.vulkan.assets.texture;

import java.nio.file.Path;

import fi.jakojaannos.riista.view.assets.Texture;
import fi.jakojaannos.riista.vulkan.internal.GPUImage;
import fi.jakojaannos.riista.vulkan.internal.device.DeviceContext;
import fi.jakojaannos.riista.vulkan.rendering.ImageView;
import fi.jakojaannos.riista.vulkan.internal.types.VkImageAspectFlags;
import fi.jakojaannos.riista.vulkan.internal.types.VkImageTiling;
import fi.jakojaannos.riista.vulkan.internal.types.VkImageUsageFlags;
import fi.jakojaannos.riista.vulkan.internal.types.VkMemoryPropertyFlags;

import static fi.jakojaannos.riista.utilities.BitMask.bitMask;

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
