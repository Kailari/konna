package fi.jakojaannos.roguelite.engine.view.rendering.mesh;

import java.nio.ByteBuffer;

public interface Mesh extends AutoCloseable {
    void setElements(int... indices);

    void setVertexData(ByteBuffer vertexData);

    void updateVertexData(
            ByteBuffer vertexData,
            int destOffsetInBytes,
            int vertexCount
    );

    default void updateVertexData(final ByteBuffer vertexData) {
        updateVertexData(vertexData, 0, vertexData.limit());
    }

    void draw(int nIndices);

    void drawAsPoints(int nIndices);

    void startDrawing();
}
