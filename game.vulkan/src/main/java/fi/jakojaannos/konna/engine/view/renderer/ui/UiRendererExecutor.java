package fi.jakojaannos.konna.engine.view.renderer.ui;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.VkExtent2D;

import java.nio.IntBuffer;

import fi.jakojaannos.konna.engine.application.PresentableState;
import fi.jakojaannos.konna.engine.assets.AssetManager;
import fi.jakojaannos.konna.engine.assets.Font;
import fi.jakojaannos.konna.engine.assets.FontTexture;
import fi.jakojaannos.konna.engine.assets.Mesh;
import fi.jakojaannos.konna.engine.util.RecreateCloseable;
import fi.jakojaannos.konna.engine.view.ui.Alignment;
import fi.jakojaannos.konna.engine.vulkan.RenderingBackend;
import fi.jakojaannos.konna.engine.vulkan.command.CommandBuffer;
import fi.jakojaannos.konna.engine.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.konna.engine.vulkan.rendering.RenderPass;
import fi.jakojaannos.konna.engine.vulkan.types.VkPrimitiveTopology;
import fi.jakojaannos.konna.engine.vulkan.window.Window;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class UiRendererExecutor extends RecreateCloseable {
    private final Mesh quadMesh;
    private final GraphicsPipeline<UiQuadVertex> quadPipeline;

    private final float contentScaleX;
    private final float contentScaleY;
    private final Font font;

    private final VkExtent2D swapchainExtent;

    public UiRendererExecutor(
            final RenderingBackend backend,
            final Window window,
            final RenderPass renderPass,
            final AssetManager assetManager
    ) {
        this.swapchainExtent = backend.swapchain().getExtent();
        this.font = assetManager.getStorage(Font.class)
                                .getOrDefault("fonts/VCR_OSD_MONO.ttf");

        final var quadVertices = new UiQuadVertex[]{
                new UiQuadVertex(new Vector2f(0, 0)),
                new UiQuadVertex(new Vector2f(1, 0)),
                new UiQuadVertex(new Vector2f(1, 1)),
                new UiQuadVertex(new Vector2f(0, 1))
        };
        final var quadIndices = new Integer[]{
                2, 1, 0,
                0, 3, 2
        };


        this.quadMesh = Mesh.from(backend,
                                  UiQuadVertex.FORMAT,
                                  quadVertices,
                                  quadIndices,
                                  null);

        this.quadPipeline = new GraphicsPipeline<>(backend.deviceContext(),
                                                   backend.swapchain(),
                                                   renderPass,
                                                   assetManager,
                                                   "shaders/vulkan/ui/quad.vert",
                                                   "shaders/vulkan/ui/quad.frag",
                                                   VkPrimitiveTopology.TRIANGLE_LIST,
                                                   UiQuadVertex.FORMAT);

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
            final CommandBuffer commandBuffer
    ) {
        drawQuads(state, commandBuffer);
        drawText(state, commandBuffer);
    }

    private void drawText(final PresentableState state, final CommandBuffer commandBuffer) {
        try (final var stack = stackPush()) {
            final var pushConstantData = stack.malloc((16 + 4) * Float.BYTES);
            final var modelMatrix = new Matrix4f();

            // TODO: Bind font rendering pipeline

            final var pCodePoint = stack.mallocInt(1);
            final var pX = stack.floats(0.0f);
            final var pY = stack.floats(0.0f);

            final var factorX = 1.0f / this.contentScaleX;
            final var factorY = 1.0f / this.contentScaleY;

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

                // TODO: Update and bind the font texture descriptor

                final var string = entry.compileString(state.uiVariables());
                final var x = align(entry.quad, string, entry.alignment, fontTexture, this.swapchainExtent.width());
                final var y = entry.quad.y;

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
                    final var renderableCharacter = fontTexture.getNextCharacterAndAdvance(codePoint,
                                                                                           pCodePoint,
                                                                                           pX, pY,
                                                                                           i, to,
                                                                                           string,
                                                                                           factorX);
                    final var framebufferHalfW = this.swapchainExtent.width() / 2.0;
                    final var framebufferHalfH = this.swapchainExtent.height() / 2.0;

                    final var x0 = x + scale(cpX, renderableCharacter.x0(), factorX) / framebufferHalfW;
                    final var x1 = x + scale(cpX, renderableCharacter.x1(), factorX) / framebufferHalfW;
                    final var y0 = y + (fontSize + scale(lineY, renderableCharacter.y0(), factorY)) / framebufferHalfH;
                    final var y1 = y + (fontSize + scale(lineY, renderableCharacter.y1(), factorY)) / framebufferHalfH;

                    final var w = (float) (x1 - x0);
                    final var h = (float) (y1 - y0);

                    modelMatrix.identity()
                               .translate((float) x0,
                                          (float) y0,
                                          (100.0f - entry.quad.z - 0.5f) / 100.0f)
                               .scale(w, h, 1.0f);

                    modelMatrix.get(0, pushConstantData);
                    entry.color.getRGBA(16 * Float.BYTES, pushConstantData);

                    // TODO: Use actual text pipeline and push UVs too
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
        this.quadPipeline.tryRecreate();
    }

    @Override
    protected void cleanup() {
    }

    @Override
    public void close() {
        super.close();
        this.quadPipeline.close();
        this.quadMesh.close();
    }

    private static float align(
            final UiRendererRecorder.QuadEntry quad,
            final String string,
            final Alignment alignment,
            final FontTexture fontTexture,
            final int framebufferWidth
    ) {
        return (float) switch (alignment) {
            case LEFT -> quad.x;
            case RIGHT -> quad.x + quad.w - scaledStringWidth(string, fontTexture, framebufferWidth);
            case CENTER -> quad.x + (quad.w - scaledStringWidth(string, fontTexture, framebufferWidth)) / 2.0f;
        };
    }

    private static float scaledStringWidth(
            final String string,
            final FontTexture fontTexture,
            final int framebufferWidth
    ) {
        return fontTexture.calculateStringWidthInPixels(string) / (framebufferWidth / 2.0f);
    }

    private static double scale(
            final double center,
            final double offset,
            final double factor
    ) {
        return (offset - center) * factor + center;
    }
}
