package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.mesh;

import fi.jakojaannos.roguelite.engine.view.rendering.mesh.Mesh;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormat;
import lombok.val;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class LWJGLMesh implements AutoCloseable, Mesh {
    private final int vao;
    private final int ebo;
    private final int vbo;
    private final VertexFormat vertexFormat;

    public LWJGLMesh(final VertexFormat vertexFormat) {
        this.vertexFormat = vertexFormat;

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        val buffers = new int[2];
        glGenBuffers(buffers);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo = buffers[0]);
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo = buffers[1]);
        vertexFormat.apply();
    }

    @Override
    public void setElements(final int... indices) {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
    }

    @Override
    public void setVertexData(final ByteBuffer vertexData) {
        glBindVertexArray(this.vao);
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
        this.vertexFormat.apply();
    }

    @Override
    public void updateVertexData(
            final ByteBuffer vertexData,
            final int destOffsetInBytes,
            final int vertexCount
    ) {
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);

        val limitBefore = vertexData.limit();
        vertexData.limit(vertexCount * this.vertexFormat.getSizeInBytes());
        glBufferSubData(GL_ARRAY_BUFFER, destOffsetInBytes, vertexData);
        vertexData.limit(limitBefore);
    }

    @Override
    public void startDrawing() {
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
    public void close() {
        glDeleteVertexArrays(this.vao);
        glDeleteBuffers(this.vbo);
        glDeleteBuffers(this.ebo);
    }
}
