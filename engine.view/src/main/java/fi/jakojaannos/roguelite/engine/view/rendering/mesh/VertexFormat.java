package fi.jakojaannos.roguelite.engine.view.rendering.mesh;

import java.util.Arrays;

public class VertexFormat {
    private final int sizeInBytes;
    private final VertexAttribute[] vertexAttributes;

    public int getSizeInBytes() {
        return this.sizeInBytes;
    }

    public VertexFormat(final VertexAttribute... vertexAttributes) {
        this.sizeInBytes = Arrays.stream(vertexAttributes)
                                 .mapToInt(VertexAttribute::getSizeInBytes)
                                 .sum();
        this.vertexAttributes = vertexAttributes;
    }

    public void apply() {
        for (int i = 0, offset = 0; i < this.vertexAttributes.length; ++i) {
            final var attribute = this.vertexAttributes[i];
            attribute.apply(i, offset, this.sizeInBytes);
            offset += attribute.getSizeInBytes();
        }
    }
}
