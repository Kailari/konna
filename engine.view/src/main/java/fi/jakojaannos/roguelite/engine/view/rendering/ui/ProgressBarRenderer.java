package fi.jakojaannos.roguelite.engine.view.rendering.ui;

import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.Mesh;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexAttribute;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.EngineUniformBufferObjectIndices;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.ShaderProgram;
import lombok.val;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.stream.IntStream;

public class ProgressBarRenderer implements AutoCloseable {
    private static final int SIZE_IN_BYTES = 4 * 4;
    private static final int MAX_PER_BATCH = 256 / 8;

    private final ShaderProgram shader;

    private final ByteBuffer vertexDataBuffer;
    private final Mesh mesh;

    public ProgressBarRenderer(
            final Path assetRoot,
            final RenderingBackend backend
    ) {
        this.shader = backend.createShaderProgram()
                             .vertexShader(assetRoot.resolve("shaders/progressbar.vert"))
                             .attributeLocation(0, "in_pos")
                             .attributeLocation(1, "in_health")
                             .attributeLocation(2, "in_maxHealth")
                             .geometryShader(assetRoot.resolve("shaders/progressbar.geom"))
                             .fragmentShader(assetRoot.resolve("shaders/progressbar.frag"))
                             .fragmentDataLocation(0, "out_fragColor")
                             .build();

        this.shader.use();
        this.shader.bindUniformBlock("CameraInfo", EngineUniformBufferObjectIndices.CAMERA);

        val vertexFormat = backend.createVertexFormat()
                                  .withAttribute(VertexAttribute.Type.FLOAT, 2, false)
                                  .withAttribute(VertexAttribute.Type.FLOAT, 1, false)
                                  .withAttribute(VertexAttribute.Type.FLOAT, 1, false)
                                  .build();
        this.mesh = backend.createMesh(vertexFormat);
        this.vertexDataBuffer = MemoryUtil.memAlloc(MAX_PER_BATCH * SIZE_IN_BYTES);
        this.mesh.setElements(IntStream.range(0, MAX_PER_BATCH)
                                       .toArray());

        this.mesh.setVertexData(this.vertexDataBuffer);
    }

    public void render(
            final double x,
            final double y,
            final double width,
            final double height,
            final double progress,
            final double maxProgress
    ) {
        this.shader.use();
        this.shader.setUniform2f("progressBarSize", (float) width, (float) height);

        this.mesh.startDrawing();
        queueVertex(x, y, progress, maxProgress);
        this.mesh.updateVertexData(this.vertexDataBuffer, 0, 1);
        this.mesh.drawAsPoints(1);
    }

    private void queueVertex(
            final double x,
            final double y,
            final double progress,
            final double maxProgress
    ) {
        this.vertexDataBuffer.putFloat(0, (float) x);
        this.vertexDataBuffer.putFloat(4, (float) y);
        this.vertexDataBuffer.putFloat(8, (float) progress);
        this.vertexDataBuffer.putFloat(12, (float) maxProgress);
    }

    @Override
    public void close() throws Exception {
        this.mesh.close();
        this.shader.close();
        MemoryUtil.memFree(this.vertexDataBuffer);
    }
}
