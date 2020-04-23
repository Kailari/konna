package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.mesh;

import java.nio.ByteBuffer;

import fi.jakojaannos.roguelite.engine.view.rendering.mesh.Mesh;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormat;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.system.MemoryUtil.NULL;

public class LWJGLMesh implements Mesh {
    private final int vao;
    private final int ebo;
    private final int vbo;

    private final int instanceVbo;
    private final VertexFormat vertexFormat;
    private float pointSize = 1.0f;

    private int vertexCount;
    private int nIndices;

    @Override
    public int getIndexCount() {
        return this.nIndices;
    }

    @Override
    public void setPointSize(final float pointSize) {
        this.pointSize = pointSize;
    }

    @Override
    public void setElements(final int... indices) {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_DYNAMIC_DRAW);
        this.nIndices = indices.length;
    }

    @Override
    public void setVertexData(final ByteBuffer vertexData) {
        glBindVertexArray(this.vao);
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        final var byteCount = vertexData.remaining();
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_DYNAMIC_DRAW);
        this.vertexFormat.apply();
        this.vertexCount = byteCount / this.vertexFormat.getSizeInBytes();
    }

    @Override
    public void setInstanceData(final ByteBuffer buffer) {
        glBindVertexArray(this.vao);
        glBindBuffer(GL_ARRAY_BUFFER, this.instanceVbo);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW);
        this.vertexFormat.applyInstanced();
    }

    public LWJGLMesh(final VertexFormat vertexFormat) {
        this.vertexFormat = vertexFormat;

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        final var buffers = new int[vertexFormat.isInstanced() ? 3 : 2];
        glGenBuffers(buffers);
        this.ebo = buffers[0];
        this.vbo = buffers[1];

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        vertexFormat.apply();

        if (vertexFormat.isInstanced()) {
            this.instanceVbo = buffers[2];
            glBindBuffer(GL_ARRAY_BUFFER, this.instanceVbo);
            vertexFormat.applyInstanced();
        } else {
            this.instanceVbo = 0;
        }
    }

    @Override
    public void updateVertexData(
            final ByteBuffer vertexData,
            final int destOffsetInBytes,
            final int vertexCount
    ) {
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);

        final var limitBefore = vertexData.limit();
        vertexData.limit(vertexCount * this.vertexFormat.getSizeInBytes());
        glBufferSubData(GL_ARRAY_BUFFER, destOffsetInBytes, vertexData);
        vertexData.limit(limitBefore);
        this.vertexCount = vertexCount;
    }

    @Override
    public void updateInstanceData(
            final ByteBuffer buffer,
            final int offset,
            final int count
    ) {
        if (!this.vertexFormat.isInstanced()) {
            throw new IllegalStateException("Tried updating instance data of non-instanced mesh!");
        }

        glBindBuffer(GL_ARRAY_BUFFER, this.instanceVbo);

        final var limitBefore = buffer.limit();
        buffer.limit(count * this.vertexFormat.getInstanceSizeInBytes());
        glBufferSubData(GL_ARRAY_BUFFER, offset, buffer);
        buffer.limit(limitBefore);
    }

    @Override
    public void drawInstanced(final int count, final int nIndices) {
        glDrawElementsInstanced(GL_TRIANGLES, nIndices, GL_UNSIGNED_INT, NULL, count);
    }

    @Override
    public void startDrawing() {
        glPointSize(this.pointSize);
        glBindVertexArray(this.vao);
    }

    @Override
    public void draw(final int nIndices) {
        glDrawElements(GL_TRIANGLES, nIndices, GL_UNSIGNED_INT, NULL);
    }

    @Override
    public void drawAsPoints(final int nIndices) {
        glDrawElements(GL_POINTS, nIndices, GL_UNSIGNED_INT, NULL);
    }

    @Override
    public void drawAsLineLoop(final int nIndices) {
        glDrawElements(GL_LINE_LOOP, nIndices, GL_UNSIGNED_INT, NULL);
    }

    @Override
    public void close() {
        glDeleteVertexArrays(this.vao);
        glDeleteBuffers(this.vbo);
        if (this.vertexFormat.isInstanced()) {
            glDeleteBuffers(this.instanceVbo);
        }

        glDeleteBuffers(this.ebo);
    }
}
