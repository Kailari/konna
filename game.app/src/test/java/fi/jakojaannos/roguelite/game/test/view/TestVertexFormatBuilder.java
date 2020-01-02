package fi.jakojaannos.roguelite.game.test.view;

import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexAttribute;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormat;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormatBuilder;

import java.util.ArrayList;
import java.util.List;

public class TestVertexFormatBuilder implements VertexFormatBuilder {
    private final List<VertexAttribute> attributes = new ArrayList<>();

    @Override
    public VertexFormatBuilder withAttribute(
            final VertexAttribute.Type type,
            final int count,
            final boolean normalized
    ) {
        this.attributes.add(new TestVertexAttribute(type, count));
        return this;
    }

    @Override
    public VertexFormat build() {
        return new VertexFormat(this.attributes.toArray(new VertexAttribute[0]));
    }

    private static class TestVertexAttribute implements VertexAttribute {
        private final Type type;
        private final int count;

        public TestVertexAttribute(
                final Type type,
                final int count
        ) {
            this.type = type;
            this.count = count;
        }

        @Override
        public int getSizeInBytes() {
            switch (this.type) {
                case BYTE:
                case UNSIGNED_BYTE:
                    return this.count;
                case SHORT:
                case UNSIGNED_SHORT:
                    return 2 * this.count;
                case INT:
                case UNSIGNED_INT:
                case FLOAT:
                    return 4 * this.count;
                case DOUBLE:
                    return 8 * this.count;
                default:
                    throw new IllegalArgumentException("Unknown type: " + this.type);
            }
        }

        @Override
        public void apply(final int i, final int offset, final int stride) {
            // NO-OP
        }
    }
}
