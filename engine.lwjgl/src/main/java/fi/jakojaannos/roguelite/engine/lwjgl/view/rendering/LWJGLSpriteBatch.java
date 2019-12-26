package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering;

import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.mesh.Mesh;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.mesh.VertexAttribute;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.mesh.VertexFormat;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.shader.ShaderProgram;
import fi.jakojaannos.roguelite.engine.utilities.math.RotatedRectangle;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatchBase;
import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;

@Slf4j
public class LWJGLSpriteBatch extends SpriteBatchBase {
    private static final Matrix4f DEFAULT_TRANSFORM = new Matrix4f().identity();
    private static final int MAX_SPRITES_PER_BATCH = 4096;
    private static final int VERTICES_PER_SPRITE = 4;
    private static final int SIZE_IN_BYTES = (2 + 2 + 3) * 4;

    private final RotatedRectangle tmpRectangle = new RotatedRectangle();
    private final Vector2d tmpVertex = new Vector2d();

    private static final VertexFormat VERTEX_FORMAT = new VertexFormat(
            new VertexAttribute(VertexAttribute.Type.FLOAT, 2, false),
            new VertexAttribute(VertexAttribute.Type.FLOAT, 2, false),
            new VertexAttribute(VertexAttribute.Type.FLOAT, 3, false)
    );

    private final Mesh batchMesh;

    private final ShaderProgram shader;
    private final int uniformModelMatrix;

    private final ByteBuffer vertexDataBuffer;

    public LWJGLSpriteBatch(
            final Path assetRoot,
            final String shader
    ) {
        super(MAX_SPRITES_PER_BATCH);

        this.shader = createShader(assetRoot, shader);
        this.uniformModelMatrix = this.shader.getUniformLocation("model");
        int uniformCameraInfoBlock = glGetUniformBlockIndex(this.shader.getShaderProgram(), "CameraInfo");
        glUniformBlockBinding(this.shader.getShaderProgram(), uniformCameraInfoBlock, UniformBufferObjectIndices.CAMERA);

        this.batchMesh = new Mesh(VERTEX_FORMAT);
        this.batchMesh.setElements(constructIndicesArray());

        this.vertexDataBuffer = MemoryUtil.memAlloc(MAX_SPRITES_PER_BATCH * VERTICES_PER_SPRITE * SIZE_IN_BYTES);
        for (int i = 0; i < MAX_SPRITES_PER_BATCH * VERTICES_PER_SPRITE; ++i) {
            updateVertex(i * SIZE_IN_BYTES, 0, 0, 0, 0, 0, 0, 0);
        }
        this.batchMesh.setVertexData(this.vertexDataBuffer);
    }

    @Override
    protected void queueFrame(
            final TextureRegion region,
            final double x,
            final double y,
            final double originX,
            final double originY,
            final double width,
            final double height,
            final double rotation
    ) {
        val offset = getNFrames() * VERTICES_PER_SPRITE * SIZE_IN_BYTES;
        tmpRectangle.set(x, y, originX, originY, width, height, rotation);
        tmpRectangle.getTopLeft(tmpVertex);
        updateVertex(offset,
                     tmpVertex.x, tmpVertex.y,
                     (float) region.getU0(), (float) region.getV0(),
                     1.0f, 1.0f, 1.0f);
        tmpRectangle.getTopRight(tmpVertex);
        updateVertex(offset + SIZE_IN_BYTES,
                     tmpVertex.x, tmpVertex.y,
                     (float) region.getU1(), (float) region.getV0(),
                     1.0f, 1.0f, 1.0f);
        tmpRectangle.getBottomRight(tmpVertex);
        updateVertex(offset + (2 * SIZE_IN_BYTES),
                     tmpVertex.x, tmpVertex.y,
                     (float) region.getU1(), (float) region.getV1(),
                     1.0f, 1.0f, 1.0f);
        tmpRectangle.getBottomLeft(tmpVertex);
        updateVertex(offset + (3 * SIZE_IN_BYTES),
                     tmpVertex.x, tmpVertex.y,
                     (float) region.getU0(), (float) region.getV1(),
                     1.0f, 1.0f, 1.0f);
    }

    private void updateVertex(
            int offset,
            double x,
            double y,
            double u,
            double v,
            float r,
            float g,
            float b
    ) {
        this.vertexDataBuffer.putFloat(offset, (float) x);
        this.vertexDataBuffer.putFloat(offset + 4, (float) y);
        this.vertexDataBuffer.putFloat(offset + 8, (float) u);
        this.vertexDataBuffer.putFloat(offset + 12, (float) v);
        this.vertexDataBuffer.putFloat(offset + 16, r);
        this.vertexDataBuffer.putFloat(offset + 20, g);
        this.vertexDataBuffer.putFloat(offset + 24, b);
    }

    @Override
    protected void flush(
            final Texture texture,
            @Nullable final Matrix4f transformation
    ) {
        this.shader.use();
        texture.use();

        int uniformCameraInfoBlock = glGetUniformBlockIndex(this.shader.getShaderProgram(), "CameraInfo");
        glUniformBlockBinding(this.shader.getShaderProgram(), uniformCameraInfoBlock, UniformBufferObjectIndices.CAMERA);
        this.shader.setUniformMat4x4(this.uniformModelMatrix,
                                     transformation != null
                                             ? transformation
                                             : DEFAULT_TRANSFORM);

        this.batchMesh.updateVertexData(this.vertexDataBuffer,
                                        0,
                                        getNFrames() * VERTICES_PER_SPRITE);

        this.batchMesh.draw(getNFrames() * 6);
    }

    @Override
    public void close() {
        MemoryUtil.memFree(this.vertexDataBuffer);
        this.batchMesh.close();
        this.shader.close();
    }

    private int[] constructIndicesArray() {
        val indices = new int[MAX_SPRITES_PER_BATCH * 6];
        for (int i = 0, j = 0; i < indices.length; i += 6, j += 4) {
            indices[i] = j;
            indices[i + 1] = j + 1;
            indices[i + 2] = j + 2;
            indices[i + 3] = j + 2;
            indices[i + 4] = j + 3;
            indices[i + 5] = j;
        }
        return indices;
    }

    private static ShaderProgram createShader(final Path assetRoot, final String shader) {
        return ShaderProgram.builder()
                            .vertexShader(assetRoot.resolve("shaders").resolve(shader + ".vert"))
                            .fragmentShader(assetRoot.resolve("shaders").resolve(shader + ".frag"))
                            .attributeLocation(0, "in_pos")
                            .attributeLocation(1, "in_uv")
                            .attributeLocation(2, "in_tint")
                            .fragmentDataLocation(0, "out_fragColor")
                            .build();
    }
}
