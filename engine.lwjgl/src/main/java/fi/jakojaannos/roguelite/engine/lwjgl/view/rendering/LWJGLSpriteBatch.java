package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering;

import lombok.extern.slf4j.Slf4j;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.utilities.math.RotatedRectangle;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.Mesh;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexAttribute;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormat;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.EngineUniformBufferObjectIndices;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.ShaderProgram;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.SpriteBatchBase;

@Slf4j
public class LWJGLSpriteBatch extends SpriteBatchBase {
    private static final Matrix4f DEFAULT_TRANSFORM = new Matrix4f().identity();
    private static final int MAX_SPRITES_PER_BATCH = 4096;
    private static final int VERTICES_PER_SPRITE = 4;
    private static final int SIZE_IN_BYTES = (2 + 2 + 3) * 4;

    private final RotatedRectangle tmpRectangle = new RotatedRectangle();
    private final Vector2d tmpVertex = new Vector2d();

    private final Mesh batchMesh;
    private final ShaderProgram shader;

    private final ByteBuffer vertexDataBuffer;

    public LWJGLSpriteBatch(
            final Path assetRoot,
            final String shader,
            final RenderingBackend backend
    ) {
        super(MAX_SPRITES_PER_BATCH);

        final VertexFormat vertexFormat = backend.createVertexFormat()
                                                 .withAttribute(VertexAttribute.Type.FLOAT, 2, false)
                                                 .withAttribute(VertexAttribute.Type.FLOAT, 2, false)
                                                 .withAttribute(VertexAttribute.Type.FLOAT, 3, false)
                                                 .build();

        this.shader = createShader(assetRoot, shader, backend);
        this.shader.bindUniformBlock("CameraInfo", EngineUniformBufferObjectIndices.CAMERA);

        this.batchMesh = backend.createMesh(vertexFormat);
        this.batchMesh.setElements(constructIndicesArray());

        this.vertexDataBuffer = MemoryUtil.memAlloc(MAX_SPRITES_PER_BATCH * VERTICES_PER_SPRITE * SIZE_IN_BYTES);
        for (int i = 0; i < MAX_SPRITES_PER_BATCH * VERTICES_PER_SPRITE; ++i) {
            updateVertex(i * SIZE_IN_BYTES, 0, 0, 0, 0, 0, 0, 0);
        }
        this.batchMesh.setVertexData(this.vertexDataBuffer);
    }

    private static ShaderProgram createShader(
            final Path assetRoot,
            final String shader,
            final RenderingBackend backend
    ) {
        return backend.createShaderProgram()
                      .vertexShader(assetRoot.resolve("shaders").resolve(shader + ".vert"))
                      .fragmentShader(assetRoot.resolve("shaders").resolve(shader + ".frag"))
                      .attributeLocation(0, "in_pos")
                      .attributeLocation(1, "in_uv")
                      .attributeLocation(2, "in_tint")
                      .fragmentDataLocation(0, "out_fragColor")
                      .build();
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
        final var offset = getNFrames() * VERTICES_PER_SPRITE * SIZE_IN_BYTES;
        this.tmpRectangle.set(x, y, originX, originY, width, height, rotation);
        this.tmpRectangle.getTopLeft(this.tmpVertex);
        updateVertex(offset,
                     this.tmpVertex.x, this.tmpVertex.y,
                     (float) region.getU0(), (float) region.getV0(),
                     1.0f, 1.0f, 1.0f);
        this.tmpRectangle.getTopRight(this.tmpVertex);
        updateVertex(offset + SIZE_IN_BYTES,
                     this.tmpVertex.x, this.tmpVertex.y,
                     (float) region.getU1(), (float) region.getV0(),
                     1.0f, 1.0f, 1.0f);
        this.tmpRectangle.getBottomRight(this.tmpVertex);
        updateVertex(offset + (2 * SIZE_IN_BYTES),
                     this.tmpVertex.x, this.tmpVertex.y,
                     (float) region.getU1(), (float) region.getV1(),
                     1.0f, 1.0f, 1.0f);
        this.tmpRectangle.getBottomLeft(this.tmpVertex);
        updateVertex(offset + (3 * SIZE_IN_BYTES),
                     this.tmpVertex.x, this.tmpVertex.y,
                     (float) region.getU0(), (float) region.getV1(),
                     1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void queueFrameUnrotated(
            final TextureRegion textureRegion,
            final double x0,
            final double y0,
            final double x1,
            final double y1,
            final double r,
            final double g,
            final double b
    ) {
        final var offset = getNFrames() * VERTICES_PER_SPRITE * SIZE_IN_BYTES;
        updateVertex(offset,
                     x0, y0,
                     textureRegion.getU0(), textureRegion.getV0(),
                     r, g, b);
        updateVertex(offset + SIZE_IN_BYTES,
                     x1, y0,
                     textureRegion.getU1(), textureRegion.getV0(),
                     r, g, b);
        updateVertex(offset + (2 * SIZE_IN_BYTES),
                     x1, y1,
                     textureRegion.getU1(), textureRegion.getV1(),
                     r, g, b);
        updateVertex(offset + (3 * SIZE_IN_BYTES),
                     x0, y1,
                     textureRegion.getU0(), textureRegion.getV1(),
                     r, g, b);
    }

    private void updateVertex(
            final int offset,
            final double x,
            final double y,
            final double u,
            final double v,
            final double r,
            final double g,
            final double b
    ) {
        this.vertexDataBuffer.putFloat(offset, (float) x);
        this.vertexDataBuffer.putFloat(offset + 4, (float) y);
        this.vertexDataBuffer.putFloat(offset + 8, (float) u);
        this.vertexDataBuffer.putFloat(offset + 12, (float) v);
        this.vertexDataBuffer.putFloat(offset + 16, (float) r);
        this.vertexDataBuffer.putFloat(offset + 20, (float) g);
        this.vertexDataBuffer.putFloat(offset + 24, (float) b);
    }

    @Override
    protected void flush(
            final Texture texture,
            @Nullable final Matrix4f transformation
    ) {
        this.shader.use();
        texture.use();
        this.batchMesh.startDrawing();

        this.shader.setUniformMat4x4("model",
                                     transformation != null
                                             ? transformation
                                             : DEFAULT_TRANSFORM);

        this.batchMesh.updateVertexData(this.vertexDataBuffer,
                                        0,
                                        getNFrames() * VERTICES_PER_SPRITE);

        this.batchMesh.draw(getNFrames() * 6);
    }

    @Override
    public void close() throws Exception {
        MemoryUtil.memFree(this.vertexDataBuffer);
        this.batchMesh.close();
        this.shader.close();
    }

    private int[] constructIndicesArray() {
        final var indices = new int[MAX_SPRITES_PER_BATCH * 6];
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
}
