package fi.jakojaannos.roguelite.engine.view.rendering.mesh;

public interface VertexAttribute {
    int getSizeInBytes();

    void apply(int i, int offset, int stride);

    enum Type {
        FLOAT,
        DOUBLE,
        INT,
        UNSIGNED_INT,
        BYTE,
        UNSIGNED_BYTE,
        SHORT,
        UNSIGNED_SHORT
    }
}
