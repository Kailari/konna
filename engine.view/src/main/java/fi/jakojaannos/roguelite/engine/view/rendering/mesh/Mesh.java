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

    void draw(int nIndices);

    void drawAsPoints(int nIndices);

    void drawAsLineLoop(int nIndices);

    void startDrawing();

    void setPointSize(float value);
}
