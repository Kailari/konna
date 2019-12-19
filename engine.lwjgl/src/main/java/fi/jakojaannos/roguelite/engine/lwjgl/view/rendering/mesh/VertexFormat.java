package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.mesh;

import lombok.Getter;
import lombok.val;

import java.util.Arrays;

public class VertexFormat {
    @Getter private final int sizeInBytes;
    private final VertexAttribute[] vertexAttributes;

    public VertexFormat(final VertexAttribute... vertexAttributes) {
        this.sizeInBytes = Arrays.stream(vertexAttributes)
                                 .mapToInt(VertexAttribute::getSizeInBytes)
                                 .sum();
        this.vertexAttributes = vertexAttributes;
    }

    public void apply() {
        for (int i = 0, offset = 0; i < this.vertexAttributes.length; ++i) {
            val attribute = this.vertexAttributes[i];
            attribute.apply(i, offset, this.sizeInBytes);
            offset += attribute.getSizeInBytes();
        }
    }
}
