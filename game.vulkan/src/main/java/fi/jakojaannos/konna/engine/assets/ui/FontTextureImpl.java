package fi.jakojaannos.konna.engine.assets.ui;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import fi.jakojaannos.konna.engine.assets.Font;
import fi.jakojaannos.konna.engine.assets.FontTexture;
import fi.jakojaannos.konna.engine.assets.RenderableCharacter;
import fi.jakojaannos.konna.engine.assets.Texture;
import fi.jakojaannos.konna.engine.assets.texture.TextureImpl;
import fi.jakojaannos.konna.engine.vulkan.GPUBuffer;
import fi.jakojaannos.konna.engine.vulkan.GPUImage;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.engine.vulkan.types.VkFormat;
import fi.jakojaannos.konna.engine.vulkan.types.VkImageTiling;
import fi.jakojaannos.konna.engine.vulkan.types.VkImageUsageFlags;
import fi.jakojaannos.konna.engine.vulkan.types.VkMemoryPropertyFlags;

import static fi.jakojaannos.konna.engine.util.BitMask.bitMask;
import static fi.jakojaannos.konna.engine.view.renderer.ui.UiRendererExecutor.getCP;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class FontTextureImpl implements FontTexture {
    private static final Logger LOG = LoggerFactory.getLogger(FontTextureImpl.class);

    private static final int FIRST_CHAR = 32;

    private final Font font;
    private final STBTTFontinfo fontInfo;
    private final int fontSize;
    private final float pixelHeightScale;

    private final int scaledBitmapW;
    private final int scaledBitmapH;
    private final Texture texture;

    // FIXME: This should likely be part of the renderer, not the font
    private final STBTTAlignedQuad alignedQuad;
    private final STBTTBakedChar.Buffer bakedCharacters;

    @Override
    public float getPixelHeightScale() {
        return 0;
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

        this.scaledBitmapW = Math.round(512 * contentScaleX);
        this.scaledBitmapH = Math.round(512 * contentScaleY);
        this.pixelHeightScale = stbtt_ScaleForPixelHeight(this.fontInfo, this.fontSize);
        this.alignedQuad = STBTTAlignedQuad.malloc();

        this.bakedCharacters = STBTTBakedChar.malloc(96); // 96 ???
        final var pixelCount = this.scaledBitmapW * this.scaledBitmapH;
        final var bitmap = BufferUtils.createByteBuffer(pixelCount);
        stbtt_BakeFontBitmap(rawTTF,
                             this.fontSize * contentScaleY,
                             bitmap,
                             this.scaledBitmapW,
                             this.scaledBitmapH,
                             FIRST_CHAR,
                             this.bakedCharacters);

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
                                                     pixelCount,
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
    // FIXME: This should be in the renderer
    public RenderableCharacter getNextCharacterAndAdvance(
            final int codePoint,
            final IntBuffer pCodePoint,
            final FloatBuffer pX,
            final FloatBuffer pY,
            final int i,
            final int to,
            final String string,
            final float factorX
    ) {
        final var cpX = pX.get(0);
        this.alignedQuad.clear();
        stbtt_GetBakedQuad(this.bakedCharacters,
                           this.scaledBitmapW,
                           this.scaledBitmapH,
                           codePoint - FIRST_CHAR,
                           pX,
                           pY,
                           this.alignedQuad,
                           true);
        pX.put(0, (float) scale(cpX, pX.get(0), factorX));
        if (this.font.isKerningEnabled() && i < to) {
            getCP(string, to, i, pCodePoint);
            final int kernAdvance = stbtt_GetCodepointKernAdvance(this.fontInfo,
                                                                  codePoint,
                                                                  pCodePoint.get(0));
            pX.put(0, pX.get(0) + kernAdvance * this.pixelHeightScale);
        }

        return new RenderableCharacter(this.alignedQuad.x0(),
                                       this.alignedQuad.x1(),
                                       this.alignedQuad.y0(),
                                       this.alignedQuad.y1(),
                                       this.alignedQuad.s0(),
                                       this.alignedQuad.s1(),
                                       this.alignedQuad.t0(),
                                       this.alignedQuad.t1());
    }

    @Override
    public void close() {
        this.texture.close();
        this.alignedQuad.free();
        this.bakedCharacters.free();
    }

    // FIXME: Duplicate method, one exists in the renderer executor, too
    private double scale(
            final double center,
            final double offset,
            final double factor
    ) {
        return (offset - center) * factor + center;
    }
}
