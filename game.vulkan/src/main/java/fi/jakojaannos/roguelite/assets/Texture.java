package fi.jakojaannos.roguelite.assets;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.vulkan.GPUImage;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.ImageView;
import fi.jakojaannos.roguelite.vulkan.types.VkImageAspectFlags;
import fi.jakojaannos.roguelite.vulkan.types.VkImageTiling;
import fi.jakojaannos.roguelite.vulkan.types.VkImageUsageFlags;
import fi.jakojaannos.roguelite.vulkan.types.VkMemoryPropertyFlags;

import static fi.jakojaannos.roguelite.util.BitMask.bitMask;

public class Texture implements AutoCloseable {
    private final GPUImage image;
    private final ImageView imageView;

    public ImageView getImageView() {
        return this.imageView;
    }

    public Texture(final DeviceContext deviceContext, final Path path) {
        this.image = new GPUImage(deviceContext,
                                  path,
                                  VkImageTiling.OPTIMAL,
                                  bitMask(VkImageUsageFlags.SAMPLED_BIT),
                                  bitMask(VkMemoryPropertyFlags.DEVICE_LOCAL_BIT));
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
