package fi.jakojaannos.riista.vulkan.renderer.ui;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.VkExtent2D;

import java.nio.IntBuffer;

import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.view.assets.Font;
import fi.jakojaannos.riista.view.assets.FontTexture;
import fi.jakojaannos.riista.view.assets.Texture;
import fi.jakojaannos.riista.view.ui.Alignment;
import fi.jakojaannos.riista.view.ui.Colors;
import fi.jakojaannos.riista.vulkan.application.PresentableState;
import fi.jakojaannos.riista.vulkan.assets.mesh.MeshImpl;
import fi.jakojaannos.riista.vulkan.assets.ui.FontImpl;
import fi.jakojaannos.riista.vulkan.internal.RenderingBackend;
import fi.jakojaannos.riista.vulkan.internal.TextureSampler;
import fi.jakojaannos.riista.vulkan.internal.command.CommandBuffer;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorPool;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorSetLayout;
import fi.jakojaannos.riista.vulkan.internal.descriptor.SwapchainImageDependentDescriptorPool;
import fi.jakojaannos.riista.vulkan.internal.types.VkDescriptorPoolCreateFlags;
import fi.jakojaannos.riista.vulkan.internal.types.VkFilter;
import fi.jakojaannos.riista.vulkan.internal.types.VkPrimitiveTopology;
import fi.jakojaannos.riista.vulkan.internal.window.Window;
import fi.jakojaannos.riista.vulkan.renderer.RenderSubpass;
import fi.jakojaannos.riista.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.riista.vulkan.rendering.RenderPass;
import fi.jakojaannos.riista.vulkan.util.RecreateCloseable;

import static fi.jakojaannos.riista.utilities.BitMask.bitMask;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointKernAdvance;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class UiRendererExecutor extends RecreateCloseable {
    private static final int MAX_TEXT_ENTRIES = 16;

    private final GraphicsPipeline<UiQuadVertex> quadPipeline;
    private final MeshImpl quadMesh;

    private final DescriptorSetLayout fontTextureDescriptorLayout;
    private final DescriptorPool descriptorPool;

    private final TextureSampler textureSampler;

    private final GraphicsPipeline<TextVertex> textPipeline;
    private final FontDescriptor[] fontTextureDescriptors;
    private final MeshImpl textMesh;

    private final float contentScaleX;
    private final float contentScaleY;
    private final FontImpl font;

    private final VkExtent2D swapchainExtent;

    public UiRendererExecutor(
            final RenderingBackend backend,
            final Window window,
            final RenderPass renderPass,
            final RenderSubpass uiSubpass,
            final AssetManager assetManager
    ) {
        this.swapchainExtent = backend.swapchain().getExtent();
        this.font = (FontImpl) assetManager.getStorage(Font.class)
                                           .getOrDefault("fonts/VCR_OSD_MONO.ttf");

        final var quadVertices = new UiQuadVertex[]{
                new UiQuadVertex(new Vector2f(0, 0)),
                new UiQuadVertex(new Vector2f(1, 0)),
                new UiQuadVertex(new Vector2f(1, 1)),
                new UiQuadVertex(new Vector2f(0, 1))
        };
        final var textQuadVertices = new TextVertex[]{
                new TextVertex(new Vector2f(0, 0)),
                new TextVertex(new Vector2f(1, 0)),
                new TextVertex(new Vector2f(1, 1)),
                new TextVertex(new Vector2f(0, 1))
        };
        final var quadIndices = new Integer[]{
                2, 1, 0,
                0, 3, 2
        };


        this.quadMesh = new MeshImpl(backend,
                                     UiQuadVertex.FORMAT,
                                     quadVertices,
                                     quadIndices,
                                     null);
        this.textMesh = new MeshImpl(backend,
                                     TextVertex.FORMAT,
                                     textQuadVertices,
                                     quadIndices,
                                     null);

        this.quadPipeline = new GraphicsPipeline<>(backend.deviceContext(),
                                                   backend.swapchain(),
                                                   renderPass,
                                                   uiSubpass,
                                                   assetManager,
                                                   "shaders/vulkan/ui/quad.vert",
                                                   "shaders/vulkan/ui/quad.frag",
                                                   VkPrimitiveTopology.TRIANGLE_LIST,
                                                   UiQuadVertex.FORMAT);

        this.descriptorPool = new SwapchainImageDependentDescriptorPool(
                backend,
                MAX_TEXT_ENTRIES,
                bitMask(VkDescriptorPoolCreateFlags.FREE_DESCRIPTOR_SET_BIT),
                new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                                        () -> backend.swapchain().getImageCount() * MAX_TEXT_ENTRIES));
        this.textureSampler = new TextureSampler(backend.deviceContext(),
                                                 VkFilter.NEAREST,
                                                 VkFilter.NEAREST);

        this.fontTextureDescriptorLayout = new DescriptorSetLayout(backend.deviceContext(),
                                                                   FontDescriptor.FONT_TEXTURE_DESCRIPTOR_BINDING);
        final var defaultTexture = assetManager.getStorage(Texture.class)
                                               .getOrDefault("textures/vulkan/texture.jpg");

        this.fontTextureDescriptors = new FontDescriptor[MAX_TEXT_ENTRIES];
        for (int i = 0; i < this.fontTextureDescriptors.length; i++) {
            this.fontTextureDescriptors[i] = new FontDescriptor(backend,
                                                                defaultTexture,
                                                                this.descriptorPool,
                                                                this.fontTextureDescriptorLayout,
                                                                this.textureSampler);
        }
        this.textPipeline = new GraphicsPipeline<>(backend.deviceContext(),
                                                   backend.swapchain(),
                                                   renderPass,
                                                   uiSubpass,
                                                   assetManager,
                                                   "shaders/vulkan/ui/text.vert",
                                                   "shaders/vulkan/ui/text.frag",
                                                   VkPrimitiveTopology.TRIANGLE_LIST,
                                                   TextVertex.FORMAT,
                                                   this.fontTextureDescriptorLayout);

        try (final var stack = stackPush()) {
            final var pX = stack.mallocFloat(1);
            final var pY = stack.mallocFloat(1);
            GLFW.glfwGetWindowContentScale(window.getHandle(), pX, pY);
            this.contentScaleX = pX.get(0);
            this.contentScaleY = pY.get(0);
        }
    }

    // FIXME: Move to some utility class
    public static int getCP(
            final String string,
            final int to,
            final int i,
            final IntBuffer outCodePoint
    ) {
        final var charA = string.charAt(i);
        if (Character.isHighSurrogate(charA) && i + 1 < to) {
            final var charB = string.charAt(i + 1);
            if (Character.isLowSurrogate(charB)) {
                outCodePoint.put(0, Character.toCodePoint(charA, charB));
                return 2;
            }
        }

        outCodePoint.put(0, charA);
        return 1;
    }

    public void flush(
            final PresentableState state,
            final CommandBuffer commandBuffer,
            final int imageIndex
    ) {
        drawQuads(state, commandBuffer);
        drawText(state, commandBuffer, imageIndex);
    }

    private void drawText(
            final PresentableState state,
            final CommandBuffer commandBuffer,
            final int imageIndex
    ) {
        try (final var stack = stackPush()) {
            final var pushConstantData = stack.malloc((16 + 4 + 4) * Float.BYTES);
            final var modelMatrix = new Matrix4f();

            vkCmdBindPipeline(commandBuffer.getHandle(),
                              VK_PIPELINE_BIND_POINT_GRAPHICS,
                              this.textPipeline.getHandle());

            vkCmdBindVertexBuffers(commandBuffer.getHandle(),
                                   0,
                                   stack.longs(this.textMesh.getVertexBuffer().getHandle()),
                                   stack.longs(0L));
            vkCmdBindIndexBuffer(commandBuffer.getHandle(),
                                 this.textMesh.getIndexBuffer().getHandle(),
                                 0L,
                                 VK_INDEX_TYPE_UINT32);

            final var pCodePoint = stack.mallocInt(1);
            final var pX = stack.floats(0.0f);
            final var pY = stack.floats(0.0f);

            final var factorX = 1.0f / this.contentScaleX;
            final var factorY = 1.0f / this.contentScaleY;

            // FIXME: Re-implement using batching to dynamic vertex buffer
            //  - Use instanced rendering? Use single quad mesh and load vertex offsets from instance buffer
            //  - Can use gl_vertexIndex (or whatever it was) for offsetting the vertices instead of transform matrix
            //  - alternatively just upload 4 verts per quad, GPU memory bandwidth impact is negligible with this low
            //    vertex counts
            var entryIndex = 0;
            for (final var entry : state.textEntries()) {
                pX.put(0, 0.0f);
                pY.put(0, 0.0f);
                pCodePoint.put(0, 0);
                var lineY = 0.0f;

                final var fontSize = entry.size;

                // FIXME: Get font key from entry and getOrDefault from font assets or sth
                final var font = this.font;

                final var fontTexture = font.getForSize(fontSize);
                final var fontPixelHeightScale = fontTexture.getPixelHeightScale();

                final var fontTextureDescriptor = this.fontTextureDescriptors[entryIndex];
                ++entryIndex;

                fontTextureDescriptor.update(fontTexture, imageIndex);
                vkCmdBindDescriptorSets(commandBuffer.getHandle(),
                                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                                        this.textPipeline.getLayout(),
                                        0,
                                        stack.longs(fontTextureDescriptor.getDescriptorSet(imageIndex)),
                                        null);

                final var string = entry.compileString(state.uiVariables());
                final var x = horizontalAlign(entry.quad,
                                              string,
                                              entry.alignment != null
                                                      ? entry.alignment
                                                      : Alignment.START,
                                              fontTexture,
                                              this.swapchainExtent.width());
                final var y = verticalAlign(entry.quad,
                                            string,
                                            entry.verticalAlignment != null
                                                    ? entry.verticalAlignment
                                                    : Alignment.START,
                                            fontTexture,
                                            this.swapchainExtent.height());

                for (int i = 0, to = string.length(); i < to; ) {
                    i += getCP(string, to, i, pCodePoint);

                    final var codePoint = pCodePoint.get(0);
                    if (codePoint == '\n') {
                        pX.put(0, 0.0f);

                        final var lineOffset = font.getLineOffset() * fontPixelHeightScale;
                        final var nextLineY = pY.get(0) + lineOffset;
                        pY.put(0, nextLineY);
                        lineY = nextLineY;

                        continue;
                    } else if (codePoint < 32 || codePoint >= 128) {
                        // Just skip unsupported characters
                        continue;
                    }

                    final var cpX = pX.get(0);

                    final var quad = fontTexture.getQuadForCharacter(codePoint, pX, pY);

                    pX.put(0, scale(cpX, pX.get(0), factorX));
                    if (this.font.isKerningEnabled() && i < to) {
                        // FIXME: is this necessary?
                        getCP(string, to, i, pCodePoint);
                        final int kernAdvance = stbtt_GetCodepointKernAdvance(font.getFontInfo(),
                                                                              codePoint,
                                                                              pCodePoint.get(0));
                        pX.put(0, pX.get(0) + kernAdvance * fontTexture.getPixelHeightScale());
                    }

                    final var framebufferHalfW = this.swapchainExtent.width() / 2.0;
                    final var framebufferHalfH = this.swapchainExtent.height() / 2.0;

                    final var x0 = x + scale(cpX, quad.x0(), factorX) / framebufferHalfW;
                    final var x1 = x + scale(cpX, quad.x1(), factorX) / framebufferHalfW;
                    final var y0 = y + (fontSize + scale(lineY, quad.y0(), factorY)) / framebufferHalfH;
                    final var y1 = y + (fontSize + scale(lineY, quad.y1(), factorY)) / framebufferHalfH;

                    final var w = (float) (x1 - x0);
                    final var h = (float) (y1 - y0);

                    modelMatrix.identity()
                               .translate((float) x0,
                                          (float) y0,
                                          (100.0f - entry.quad.z - 0.5f) / 100.0f)
                               .scale(w, h, 1.0f);

                    modelMatrix.get(0, pushConstantData);
                    // FIXME: Get text color from hierarchy if null; do something like with quad colors
                    final var color = entry.color != null
                            ? entry.color
                            : Colors.WHITE;
                    color.getRGBA(16 * Float.BYTES, pushConstantData);
                    pushConstantData.putFloat(20 * Float.BYTES, quad.u0());
                    pushConstantData.putFloat(21 * Float.BYTES, quad.v0());
                    pushConstantData.putFloat(22 * Float.BYTES, quad.u1());
                    pushConstantData.putFloat(23 * Float.BYTES, quad.v1());

                    vkCmdPushConstants(commandBuffer.getHandle(),
                                       this.textPipeline.getLayout(),
                                       VK_SHADER_STAGE_VERTEX_BIT,
                                       0,
                                       pushConstantData);

                    vkCmdDrawIndexed(commandBuffer.getHandle(),
                                     this.textMesh.getIndexCount(),
                                     1,
                                     0,
                                     0,
                                     0);
                }
            }
        }
    }

    private void drawQuads(final PresentableState state, final CommandBuffer commandBuffer) {
        try (final var stack = stackPush()) {
            final var pushConstantData = stack.malloc((16 + 4) * Float.BYTES);
            final var modelMatrix = new Matrix4f();

            vkCmdBindPipeline(commandBuffer.getHandle(),
                              VK_PIPELINE_BIND_POINT_GRAPHICS,
                              this.quadPipeline.getHandle());

            vkCmdBindVertexBuffers(commandBuffer.getHandle(),
                                   0,
                                   stack.longs(this.quadMesh.getVertexBuffer().getHandle()),
                                   stack.longs(0L));
            vkCmdBindIndexBuffer(commandBuffer.getHandle(),
                                 this.quadMesh.getIndexBuffer().getHandle(),
                                 0L,
                                 VK_INDEX_TYPE_UINT32);

            for (final var entry : state.quadEntries()) {
                modelMatrix.identity()
                           .translate((float) entry.x, (float) entry.y, (100.0f - entry.z) / 100.0f)
                           .scale((float) entry.w, (float) entry.h, 1.0f);

                modelMatrix.get(0, pushConstantData);
                entry.color.getRGBA(16 * Float.BYTES, pushConstantData);

                vkCmdPushConstants(commandBuffer.getHandle(),
                                   this.quadPipeline.getLayout(),
                                   VK_SHADER_STAGE_VERTEX_BIT,
                                   0,
                                   pushConstantData);

                vkCmdDrawIndexed(commandBuffer.getHandle(),
                                 this.quadMesh.getIndexCount(),
                                 1,
                                 0,
                                 0,
                                 0);
            }
        }
    }

    @Override
    protected void recreate() {
        this.descriptorPool.tryRecreate();
        for (final var descriptor : this.fontTextureDescriptors) {
            descriptor.tryRecreate();
        }

        this.quadPipeline.tryRecreate();
        this.textPipeline.tryRecreate();
    }

    @Override
    protected void cleanup() {
    }

    @Override
    public void close() {
        super.close();
        this.textMesh.close();
        this.quadMesh.close();

        this.descriptorPool.close();
        this.textureSampler.close();

        for (final var descriptor : this.fontTextureDescriptors) {
            descriptor.close();
        }

        this.fontTextureDescriptorLayout.close();

        this.textPipeline.close();
        this.quadPipeline.close();
    }

    private static float horizontalAlign(
            final UiRendererRecorder.QuadEntry quad,
            final String string,
            final Alignment alignment,
            final FontTexture fontTexture,
            final int framebufferWidth
    ) {
        return (float) switch (alignment) {
            case START -> quad.x;
            case END -> quad.x + quad.w - scaledStringWidth(string, fontTexture, framebufferWidth);
            case CENTER -> quad.x + (quad.w - scaledStringWidth(string, fontTexture, framebufferWidth)) / 2.0f;
        };
    }

    private static float verticalAlign(
            final UiRendererRecorder.QuadEntry quad,
            final String string,
            final Alignment alignment,
            final FontTexture fontTexture,
            final int framebufferHeight
    ) {
        return (float) switch (alignment) {
            case START -> quad.y;
            case END -> quad.y + quad.h - scaledStringHeight(string, fontTexture, framebufferHeight);
            case CENTER -> quad.y + (quad.h - scaledStringHeight(string, fontTexture, framebufferHeight)) / 2.0f;
        };
    }

    private static float scaledStringWidth(
            final String string,
            final FontTexture fontTexture,
            final int framebufferWidth
    ) {
        return fontTexture.calculateStringWidthInPixels(string) / (framebufferWidth / 2.0f);
    }

    private static float scaledStringHeight(
            final String string,
            final FontTexture fontTexture,
            final int framebufferHeight
    ) {
        return fontTexture.getFontSize() / (framebufferHeight / 2.0f);
    }

    private static float scale(
            final float center,
            final float offset,
            final float factor
    ) {
        return (offset - center) * factor + center;
    }
}
