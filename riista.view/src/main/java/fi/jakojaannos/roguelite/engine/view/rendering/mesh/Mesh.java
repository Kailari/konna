package fi.jakojaannos.roguelite.engine.view.rendering.mesh;

import java.nio.ByteBuffer;

public interface Mesh extends AutoCloseable {
    int getIndexCount();

    void setElements(int... indices);

    void setVertexData(ByteBuffer vertexData);

    void setInstanceData(ByteBuffer buffer);

    void setPointSize(float value);

    void updateVertexData(ByteBuffer vertexData, int destOffsetInBytes, int vertexCount);

    void draw(int nIndices);

    void drawAsPoints(int nIndices);

    void drawAsLineLoop(int nIndices);

    void updateInstanceData(ByteBuffer buffer, int offset, int count);

    void drawInstanced(int count, final int nIndices);

    void startDrawing();
}
