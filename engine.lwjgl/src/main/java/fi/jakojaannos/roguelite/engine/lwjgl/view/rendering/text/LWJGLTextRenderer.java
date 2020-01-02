package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text;

import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.EngineUniformBufferObjectIndices;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.ShaderProgram;
import fi.jakojaannos.roguelite.engine.view.text.Font;
import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointKernAdvance;
import static org.lwjgl.system.MemoryUtil.NULL;

@Slf4j
public class LWJGLTextRenderer implements TextRenderer {
    private static final int SIZE_IN_BYTES = (2 + 2 + 3) * 4;

    private final ShaderProgram shader;

    private final ByteBuffer vertexDataBuffer;
    private final int vao;
    private final int vbo;
    private final int ebo;

    public LWJGLTextRenderer(
            final Path assetRoot,
            final RenderingBackend backend
    ) {

        this.shader = createShader(assetRoot, backend);
        this.shader.bindUniformBlock("CameraInfo", EngineUniformBufferObjectIndices.CAMERA);

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        this.vbo = glGenBuffers();
        this.ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        val indices = new int[]{
                0, 1, 2,
                3, 0, 2,
        };
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        this.vertexDataBuffer = MemoryUtil.memAlloc(4 * SIZE_IN_BYTES);
    }

    @Override
    public void draw(
            final double x,
            final double y,
            final int fontSize,
            final Font font,
            final String string
    ) {
        this.shader.use();
        this.shader.setUniformMat4x4("model", new Matrix4f().identity());
        drawInternal(x, y + fontSize, fontSize, font, string);
    }

    public void drawCentered(
            final double x,
            final double y,
            final int fontSize,
            final Font font,
            final String text
    ) {
        val textWidth = font.getStringWidthInPixels(fontSize, text);

        val textX = x - textWidth / 2.0;
        val textY = y - fontSize / 2.0;
        this.draw(textX, textY, fontSize, font, text);
    }

    private void drawInternal(
            final double x,
            final double y,
            final int fontSize,
            final Font font,
            final String string
    ) {
        glBindVertexArray(this.vao);
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        val fontTexture = font.getTextureForSize(fontSize);
        val fontPixelHeightScale = fontTexture.getPixelHeightScale();

        fontTexture.use();
        try (val stack = MemoryStack.stackPush()) {
            val pCodePoint = stack.mallocInt(1);

            val pX = stack.floats(0.0f);
            val pY = stack.floats(0.0f);

            val alignedQuad = STBTTAlignedQuad.mallocStack(stack);

            val factorX = 1.0f / fontTexture.getContentScaleX();
            val factorY = 1.0f / fontTexture.getContentScaleY();

            var lineStart = 0;
            var lineY = 0.0f;

            for (int i = 0, to = string.length(); i < to; ) {
                i += Font.getCP(string, to, i, pCodePoint);

                val cp = pCodePoint.get(0);
                if (cp == '\n') {
                    pX.put(0, 0.0f);

                    val lineOffset = font.getLineOffset() * fontPixelHeightScale;
                    val nextLineY = pY.get(0) + lineOffset;
                    pY.put(0, nextLineY);
                    lineY = nextLineY;

                    lineStart = i;
                    continue;
                } else if (cp < 32 || cp >= 128) {
                    continue;
                }

                val cpX = pX.get(0);
                ((LWJGLFontTexture) fontTexture).getNextCharacterToQuad(cp, pX, pY, alignedQuad);
                pX.put(0, (float) scale(cpX, pX.get(0), factorX));
                if (font.isKerningEnabled() && i < to) {
                    Font.getCP(string, to, i, pCodePoint);
                    pX.put(0, pX.get(0) + stbtt_GetCodepointKernAdvance(((LWJGLFont) font).getFontInfo(), cp, pCodePoint.get(0)) * fontPixelHeightScale);
                }

                val x0 = x + scale(cpX, alignedQuad.x0(), factorX);
                val x1 = x + scale(cpX, alignedQuad.x1(), factorX);
                val y0 = y + scale(lineY, alignedQuad.y0(), factorY);
                val y1 = y + scale(lineY, alignedQuad.y1(), factorY);
                val u0 = alignedQuad.s0();
                val u1 = alignedQuad.s1();
                val v0 = alignedQuad.t0();
                val v1 = alignedQuad.t1();

                updateVertex(0, x0, y0, u0, v0, 1.0f, 1.0f, 1.0f);
                updateVertex(SIZE_IN_BYTES, x1, y0, u1, v0, 1.0f, 1.0f, 1.0f);
                updateVertex(2 * SIZE_IN_BYTES, x1, y1, u1, v1, 1.0f, 1.0f, 1.0f);
                updateVertex(3 * SIZE_IN_BYTES, x0, y1, u0, v1, 1.0f, 1.0f, 1.0f);


                glBufferData(GL_ARRAY_BUFFER, this.vertexDataBuffer, GL_STATIC_DRAW);
                glEnableVertexAttribArray(0);
                glVertexAttribPointer(0,
                                      2,
                                      GL_FLOAT,
                                      false,
                                      SIZE_IN_BYTES,
                                      0); // offset: pos is first attribute -> 0
                glEnableVertexAttribArray(1);
                glVertexAttribPointer(1,
                                      2,
                                      GL_FLOAT,
                                      false,
                                      SIZE_IN_BYTES,
                                      2 * 4); // offset: pos = 2 * sizeof(float)
                glEnableVertexAttribArray(2);
                glVertexAttribPointer(2,
                                      3,
                                      GL_FLOAT,
                                      false,
                                      SIZE_IN_BYTES,
                                      4 * 4); // offset: pos + uv = 4 * sizeof(float)

                glDrawElements(GL_TRIANGLES,
                               6,
                               GL_UNSIGNED_INT,
                               NULL);
            }
        }
    }

    private void updateVertex(
            final int offset,
            final double x,
            final double y,
            final double u,
            final double v,
            final float r,
            final float g,
            final float b
    ) {
        this.vertexDataBuffer.putFloat(offset, (float) x);
        this.vertexDataBuffer.putFloat(offset + 4, (float) y);
        this.vertexDataBuffer.putFloat(offset + 8, (float) u);
        this.vertexDataBuffer.putFloat(offset + 12, (float) v);
        this.vertexDataBuffer.putFloat(offset + 16, r);
        this.vertexDataBuffer.putFloat(offset + 20, g);
        this.vertexDataBuffer.putFloat(offset + 24, b);
    }

    private double scale(
            final double center,
            final double offset,
            final double factor
    ) {
        return (offset - center) * factor + center;
    }

    private static ShaderProgram createShader(
            final Path assetRoot,
            final RenderingBackend backend
    ) {
        return backend.createShaderProgram()
                      .vertexShader(assetRoot.resolve("shaders/text.vert"))
                      .fragmentShader(assetRoot.resolve("shaders/text.frag"))
                      .attributeLocation(0, "in_pos")
                      .attributeLocation(1, "in_uv")
                      .attributeLocation(2, "in_tint")
                      .fragmentDataLocation(0, "out_fragColor")
                      .build();
    }

    @Override
    public void close() throws Exception {
        MemoryUtil.memFree(this.vertexDataBuffer);
        glDeleteVertexArrays(this.vao);
        glDeleteBuffers(this.vbo);
        glDeleteBuffers(this.ebo);

        this.shader.close();
    }
}
