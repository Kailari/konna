package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.mesh;

import lombok.val;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Mesh implements AutoCloseable {
    private final int vao;
    private final int ebo;
    private final int vbo;
    private final VertexFormat vertexFormat;

    public Mesh(final VertexFormat vertexFormat) {
        this.vertexFormat = vertexFormat;

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        val buffers = new int[2];
        glGenBuffers(buffers);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo = buffers[0]);
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo = buffers[1]);
        vertexFormat.apply();
    }

    public void setElements(final int... indices) {
        if (indices.length % 3 != 0) {
            throw new IllegalArgumentException("Number of indices must by divisible by 3!");
        }

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
    }

    public void setVertexData(final ByteBuffer vertexData) {
        glBindVertexArray(this.vao);
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
        this.vertexFormat.apply();
    }

    public void updateVertexData(final ByteBuffer vertexData) {
        updateVertexData(vertexData, 0, vertexData.limit());
    }

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

    public void draw(final int nIndices) {
        glBindVertexArray(this.vao);
        glDrawElements(GL_TRIANGLES, nIndices, GL_UNSIGNED_INT, NULL);
    }

    @Override
    public void close() {
        glDeleteVertexArrays(this.vao);
        glDeleteBuffers(this.vbo);
        glDeleteBuffers(this.ebo);
    }
}
