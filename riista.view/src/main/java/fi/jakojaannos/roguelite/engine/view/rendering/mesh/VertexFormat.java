package fi.jakojaannos.roguelite.engine.view.rendering.mesh;

import java.util.Arrays;
import java.util.function.Predicate;

public class VertexFormat {
    private final int sizeInBytes;
    private final int instanceSizeInBytes;
    private final VertexAttribute[] vertexAttributes;
    private final VertexAttribute[] instanceVertexAttributes;
    private final boolean instanced;

    public boolean isInstanced() {
        return this.instanced;
    }

    public int getInstanceSizeInBytes() {
        return this.instanceSizeInBytes;
    }

    public int getSizeInBytes() {
        return this.sizeInBytes;
    }

    public VertexFormat(final VertexAttribute... vertexAttributes) {
        this.sizeInBytes = Arrays.stream(vertexAttributes)
                                 .filter(Predicate.not(VertexAttribute::isInstanced))
                                 .mapToInt(VertexAttribute::getSizeInBytes)
                                 .sum();
        this.instanceSizeInBytes = Arrays.stream(vertexAttributes)
                                         .filter(VertexAttribute::isInstanced)
                                         .mapToInt(VertexAttribute::getSizeInBytes)
                                         .sum();

        this.vertexAttributes = Arrays.stream(vertexAttributes)
                                      .filter(Predicate.not(VertexAttribute::isInstanced))
                                      .toArray(VertexAttribute[]::new);
        this.instanceVertexAttributes = Arrays.stream(vertexAttributes)
                                              .filter(VertexAttribute::isInstanced)
                                              .toArray(VertexAttribute[]::new);

        this.instanced = this.instanceVertexAttributes.length > 0;
    }

    public void apply() {
        for (int i = 0, offset = 0; i < this.vertexAttributes.length; ++i) {
            final var attribute = this.vertexAttributes[i];
            attribute.apply(i, offset, this.sizeInBytes);
            offset += attribute.getSizeInBytes();
        }
    }

    public void applyInstanced() {
        final var baseIndex = this.vertexAttributes.length;
        for (int i = 0, offset = 0; i < this.instanceVertexAttributes.length; ++i) {
            final var attribute = this.instanceVertexAttributes[i];
            attribute.apply(baseIndex + i, offset, this.instanceSizeInBytes);
            offset += attribute.getSizeInBytes();
        }
    }
}
