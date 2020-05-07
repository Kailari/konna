package fi.jakojaannos.roguelite.vulkan.textures;

import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkMemoryRequirements;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import fi.jakojaannos.roguelite.util.BitMask;
import fi.jakojaannos.roguelite.vulkan.GPUBuffer;
import fi.jakojaannos.roguelite.vulkan.command.CommandPool;
import fi.jakojaannos.roguelite.vulkan.command.GPUQueue;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.memory.GPUMemory;
import fi.jakojaannos.roguelite.vulkan.types.VkFormat;
import fi.jakojaannos.roguelite.vulkan.types.VkImageTiling;
import fi.jakojaannos.roguelite.vulkan.types.VkMemoryPropertyFlags;

import static fi.jakojaannos.roguelite.util.BitMask.bitMask;
import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class GPUImage implements AutoCloseable {
    private final DeviceContext deviceContext;

    private final long handle;
    private final GPUMemory memory;

    private final int width;
    private final int height;
    private final VkFormat format;

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public long getHandle() {
        return this.handle;
    }

    public VkFormat getFormat() {
        return this.format;
    }

    public GPUImage(
            final DeviceContext deviceContext,
            final Path assetPath,
            final VkImageTiling tiling,
            final int usageFlags,
            final BitMask<VkMemoryPropertyFlags> memoryProperties
    ) {
        this.deviceContext = deviceContext;

        this.format = VkFormat.R8G8B8A8_SRGB;

        final GPUBuffer stagingBuffer;
        try (final var image = Image.loadFrom(assetPath)) {
            final var imageSize = image.width() * image.height() * 4;
            stagingBuffer = new GPUBuffer(deviceContext,
                                          imageSize,
                                          VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                          bitMask(VkMemoryPropertyFlags.HOST_VISIBLE_BIT, VkMemoryPropertyFlags.HOST_COHERENT_BIT));
            stagingBuffer.push(image.pixels, 0, imageSize);
            this.width = image.width();
            this.height = image.height();
        }

        this.handle = createImage(deviceContext,
                                  this.width,
                                  this.height,
                                  this.format,
                                  tiling,
                                  VK_IMAGE_USAGE_TRANSFER_DST_BIT | usageFlags);
        this.memory = allocateMemory(this.handle, deviceContext, memoryProperties);
        this.memory.bindImage(this.handle, 0);

        // Prepare the image for copy. This claims the image ownership on the transfer queue
        transitionLayout(deviceContext.getTransferCommandPool(),
                         this.format,
                         VK_QUEUE_FAMILY_IGNORED,
                         VK_QUEUE_FAMILY_IGNORED,
                         deviceContext.getTransferQueue(),
                         VK_IMAGE_LAYOUT_UNDEFINED,
                         VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);

        // Perform the copy
        stagingBuffer.copyToAndWait(deviceContext.getTransferCommandPool(),
                                    deviceContext.getTransferQueue(),
                                    this);

        // Move the ownership to graphics queue (transfer releases)
        transitionLayout(deviceContext.getTransferCommandPool(),
                         this.format,
                         deviceContext.getQueueFamilies().transfer(),
                         deviceContext.getQueueFamilies().graphics(),
                         deviceContext.getTransferQueue(),
                         VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                         VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
        // Acquire the ownership on the graphics queue (graphics acquires)
        transitionLayout(deviceContext.getGraphicsCommandPool(),
                         this.format,
                         VK_QUEUE_FAMILY_IGNORED,
                         VK_QUEUE_FAMILY_IGNORED,
                         deviceContext.getGraphicsQueue(),
                         VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                         VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

        stagingBuffer.close();
    }

    public void transitionLayout(
            final CommandPool commandPool,
            final VkFormat format,
            final int srcQueueFamilyIndex,
            final int dstQueueFamilyIndex,
            final GPUQueue queue,
            final int oldLayout,
            final int newLayout
    ) {
        final var commandBuffer = commandPool.allocate(1)[0];
        try (final var ignored = stackPush();
             final var ignored2 = commandBuffer.begin(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
        ) {
            final var barrier = VkImageMemoryBarrier
                    .callocStack(1)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                    .oldLayout(oldLayout)
                    .newLayout(newLayout)
                    .srcQueueFamilyIndex(srcQueueFamilyIndex)
                    .dstQueueFamilyIndex(dstQueueFamilyIndex)
                    .image(this.handle);
            barrier.subresourceRange()
                   .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                   .baseMipLevel(0)
                   .levelCount(1)
                   .baseArrayLayer(0)
                   .layerCount(1);

            /*
                Layout transition table:
                https://www.khronos.org/registry/vulkan/specs/1.0/html/vkspec.html#synchronization-access-types-supported
             */
            final int sourceStage;
            final int destinationStage;
            final int srcAccessMask;
            final int dstAccessMask;
            if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
                srcAccessMask = 0;
                dstAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;

                sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
            } else if (oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
                srcAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
                dstAccessMask = VK_ACCESS_SHADER_READ_BIT;

                sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;

                // FIXME: This breaks if textures are needed in non-fragment shaders (?)
                destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
            }
            // Hacky ownership transfer from transfer queue to graphics queue
            else if (newLayout == oldLayout && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
                srcAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
                dstAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;

                sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
                destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
            } else {
                throw new UnsupportedOperationException("Unsupported layout transition: [" + oldLayout + "] to [" + newLayout + "]");
            }

            barrier.srcAccessMask(srcAccessMask)
                   .dstAccessMask(dstAccessMask);

            vkCmdPipelineBarrier(commandBuffer.getHandle(),
                                 sourceStage,
                                 destinationStage,
                                 0,
                                 null,
                                 null,
                                 barrier);
        }

        queue.submit(commandBuffer);
        vkQueueWaitIdle(queue.getHandle());
        vkFreeCommandBuffers(this.deviceContext.getDevice(),
                             commandPool.getHandle(),
                             commandBuffer.getHandle());
    }

    @Override
    public void close() {
        vkDestroyImage(this.deviceContext.getDevice(), this.handle, null);
        this.memory.close();
    }

    private static long createImage(
            final DeviceContext deviceContext,
            final int textureWidth,
            final int textureHeight,
            final VkFormat format,
            final VkImageTiling tiling,
            final int usageFlags
    ) {
        try (final var stack = stackPush()) {
            final var createInfo = VkImageCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
                    .imageType(VK_IMAGE_TYPE_2D)
                    .mipLevels(1)
                    .arrayLayers(1)
                    .format(format.asInt())
                    .tiling(tiling.asInt())
                    .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                    .usage(usageFlags)
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE)
                    .samples(VK_SAMPLE_COUNT_1_BIT);
            createInfo.extent()
                      .width(textureWidth)
                      .height(textureHeight)
                      .depth(1);

            final var pHandle = stack.mallocLong(1);
            ensureSuccess(vkCreateImage(deviceContext.getDevice(),
                                        createInfo,
                                        null,
                                        pHandle),
                          "Creating image failed");
            return pHandle.get(0);
        }
    }

    private static GPUMemory allocateMemory(
            final long handle,
            final DeviceContext deviceContext,
            final BitMask<VkMemoryPropertyFlags> memoryProperties
    ) {
        try (final var ignored = stackPush()) {
            final var memoryRequirements = VkMemoryRequirements.callocStack();
            vkGetImageMemoryRequirements(deviceContext.getDevice(), handle, memoryRequirements);

            return deviceContext.getMemoryManager().allocate(memoryRequirements, memoryProperties);
        }
    }

    private static record Image(
            ByteBuffer pixels,
            int width,
            int height,
            int channels
    ) implements AutoCloseable {
        public static Image loadFrom(final Path path) {
            try (final var stack = stackPush()) {
                final var pTextureWidth = stack.mallocInt(1);
                final var pTextureHeight = stack.mallocInt(1);
                final var pTextureChannels = stack.mallocInt(1);
                final var pixels = stbi_load(path.toString(),
                                             pTextureWidth,
                                             pTextureHeight,
                                             pTextureChannels,
                                             STBI_rgb_alpha);
                if (pixels == null) {
                    throw new IllegalStateException("Loading texture \"" + path + "\" failed!");
                }

                return new Image(pixels,
                                 pTextureWidth.get(0),
                                 pTextureHeight.get(0),
                                 pTextureChannels.get(0));
            }
        }

        @Override
        public void close() {
            stbi_image_free(this.pixels);
        }
    }
}
