package fi.jakojaannos.konna.engine.assets.ui;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import fi.jakojaannos.konna.engine.assets.Font;
import fi.jakojaannos.konna.engine.assets.FontTexture;
import fi.jakojaannos.konna.engine.assets.Texture;
import fi.jakojaannos.konna.engine.assets.texture.TextureImpl;
import fi.jakojaannos.konna.engine.view.TexturedQuad;
import fi.jakojaannos.konna.engine.vulkan.GPUBuffer;
import fi.jakojaannos.konna.engine.vulkan.GPUImage;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.engine.vulkan.rendering.ImageView;
import fi.jakojaannos.konna.engine.vulkan.types.VkFormat;
import fi.jakojaannos.konna.engine.vulkan.types.VkImageTiling;
import fi.jakojaannos.konna.engine.vulkan.types.VkImageUsageFlags;
import fi.jakojaannos.konna.engine.vulkan.types.VkMemoryPropertyFlags;

import static fi.jakojaannos.konna.engine.util.BitMask.bitMask;
import static fi.jakojaannos.konna.engine.view.renderer.ui.UiRendererExecutor.getCP;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.VK10.*;

public class FontTextureImpl implements FontTexture {
    private static final int FIRST_CHAR = 32;

    private final Font font;
    private final STBTTFontinfo fontInfo;
    private final int fontSize;
    private final float pixelHeightScale;

    private final int scaledBitmapW;
    private final int scaledBitmapH;
    private final Texture texture;

    private final RenderableCharacter alignedQuad;
    private final STBTTPackedchar.Buffer packedCharacters;

    @Override
    public float getPixelHeightScale() {
        return this.pixelHeightScale;
    }

    @Override
    public ImageView getImageView() {
        return this.texture.getImageView();
    }

    @Override
    public int getFontSize() {
        return this.fontSize;
    }

    public FontTextureImpl(
            final DeviceContext deviceContext,
            final ByteBuffer rawTTF,
            final Font font,
            final STBTTFontinfo fontInfo,
            final int fontSize,
            final float contentScaleX,
            final float contentScaleY
    ) {
        this.font = font;
        this.fontInfo = fontInfo;
        this.fontSize = fontSize;

        this.scaledBitmapW = (int) Math.ceil(1024 * contentScaleX);
        this.scaledBitmapH = (int) Math.ceil(1024 * contentScaleY);
        this.pixelHeightScale = stbtt_ScaleForPixelHeight(this.fontInfo, this.fontSize);
        this.alignedQuad = RenderableCharacter.malloc();

        this.packedCharacters = STBTTPackedchar.malloc(128);

        final var pixelCount = this.scaledBitmapW * this.scaledBitmapH;
        final var bitmap = BufferUtils.createByteBuffer(pixelCount);

        try (final var context = STBTTPackContext.malloc()) {
            if (!stbtt_PackBegin(context, bitmap, this.scaledBitmapW, this.scaledBitmapH, 0, 1, NULL)) {
                throw new IllegalStateException("Starting font packing failed!");
            }

            this.packedCharacters.limit(127);
            this.packedCharacters.position(FIRST_CHAR);
            stbtt_PackSetOversampling(context, 1, 1);
            stbtt_PackFontRange(context,
                                rawTTF,
                                0,
                                this.fontSize,
                                FIRST_CHAR,
                                this.packedCharacters);

            this.packedCharacters.clear();
            stbtt_PackEnd(context);
        }

        final var format = VkFormat.R8_UINT;
        final var image = new GPUImage(deviceContext,
                                       this.scaledBitmapW,
                                       this.scaledBitmapH,
                                       format,
                                       VkImageTiling.OPTIMAL,
                                       bitMask(VkImageUsageFlags.TRANSFER_DST_BIT,
                                               VkImageUsageFlags.SAMPLED_BIT),
                                       bitMask(VkMemoryPropertyFlags.DEVICE_LOCAL_BIT));

        try (final var stagingBuffer = new GPUBuffer(deviceContext,
                                                     pixelCount * format.getSize(),
                                                     VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                     bitMask(VkMemoryPropertyFlags.HOST_VISIBLE_BIT,
                                                             VkMemoryPropertyFlags.HOST_COHERENT_BIT))
        ) {
            stagingBuffer.push(bitmap, 0, pixelCount);

            // Prepare the image for copy. This claims the image ownership on the transfer queue
            image.transitionLayout(deviceContext.getTransferCommandPool(),
                                   format,
                                   VK_QUEUE_FAMILY_IGNORED,
                                   VK_QUEUE_FAMILY_IGNORED,
                                   deviceContext.getTransferQueue(),
                                   VK_IMAGE_LAYOUT_UNDEFINED,
                                   VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);

            // Perform the copy
            stagingBuffer.copyToAndWait(deviceContext.getTransferCommandPool(),
                                        deviceContext.getTransferQueue(),
                                        image);

            // Move the ownership to graphics queue (transfer releases)
            image.transitionLayout(deviceContext.getTransferCommandPool(),
                                   format,
                                   deviceContext.getQueueFamilies().transfer(),
                                   deviceContext.getQueueFamilies().graphics(),
                                   deviceContext.getTransferQueue(),
                                   VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                                   VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
            // Acquire the ownership on the graphics queue (graphics acquires)
            image.transitionLayout(deviceContext.getGraphicsCommandPool(),
                                   format,
                                   VK_QUEUE_FAMILY_IGNORED,
                                   VK_QUEUE_FAMILY_IGNORED,
                                   deviceContext.getGraphicsQueue(),
                                   VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                                   VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        }

        this.texture = new TextureImpl(deviceContext, image);
    }

    @Override
    public float calculateStringWidthInPixels(final String string) {
        int width = 0;
        try (final var stack = stackPush()) {
            final var pCodePoint = stack.mallocInt(1);
            final var pAdvancedWidth = stack.mallocInt(1);
            final var pLeftSideBearing = stack.mallocInt(1);

            final var from = 0;
            final var to = string.length();
            int i = from;
            while (i < to) {
                i += getCP(string, to, i, pCodePoint);
                final var cp = pCodePoint.get(0);

                stbtt_GetCodepointHMetrics(this.fontInfo, cp, pAdvancedWidth, pLeftSideBearing);
                width += pAdvancedWidth.get(0);

                if (this.font.isKerningEnabled() && i < to) {
                    getCP(string, to, i, pCodePoint);
                    width += stbtt_GetCodepointKernAdvance(this.fontInfo, cp, pCodePoint.get(0));
                }
            }
        }

        return width * stbtt_ScaleForPixelHeight(this.fontInfo, this.fontSize);
    }

    @Override
    public TexturedQuad getQuadForCharacter(
            final int character,
            final FloatBuffer pX,
            final FloatBuffer pY
    ) {
        this.alignedQuad.clear();
        this.packedCharacters.position(0);

        stbtt_GetPackedQuad(this.packedCharacters,
                            this.scaledBitmapW,
                            this.scaledBitmapH,
                            character,
                            pX,
                            pY,
                            this.alignedQuad,
                            false);
        return this.alignedQuad;
    }

    @Override
    public void close() {
        this.texture.close();
        this.alignedQuad.free();
        this.packedCharacters.free();
    }
}
