package fi.jakojaannos.roguelite.game.test.view;

import java.util.ArrayList;
import java.util.List;

import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexAttribute;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormat;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormatBuilder;

public class TestVertexFormatBuilder implements VertexFormatBuilder {
    private final List<VertexAttribute> attributes = new ArrayList<>();

    @Override
    public VertexFormatBuilder withAttribute(
            final VertexAttribute.Type type,
            final int count,
            final boolean normalized
    ) {
        this.attributes.add(new TestVertexAttribute(false, type, count));
        return this;
    }

    @Override
    public VertexFormatBuilder withInstanceAttribute(
            final VertexAttribute.Type type,
            final int count,
            final boolean normalized
    ) {
        this.attributes.add(new TestVertexAttribute(true, type, count));
        return this;
    }

    @Override
    public VertexFormat build() {
        return new VertexFormat(this.attributes.toArray(new VertexAttribute[0]));
    }

    private static class TestVertexAttribute implements VertexAttribute {
        private final Type type;
        private final int count;
        private final boolean instanced;

        @Override
        public int getSizeInBytes() {
            return switch (this.type) {
                case BYTE, UNSIGNED_BYTE -> this.count;
                case SHORT, UNSIGNED_SHORT -> 2 * this.count;
                case INT, UNSIGNED_INT, FLOAT -> 4 * this.count;
                case DOUBLE -> 8 * this.count;
            };
        }

        @Override
        public boolean isInstanced() {
            return this.instanced;
        }

        public TestVertexAttribute(
                final boolean instanced,
                final Type type,
                final int count
        ) {
            this.instanced = instanced;
            this.type = type;
            this.count = count;
        }

        @Override
        public void apply(final int i, final int offset, final int stride) {
            // NO-OP
        }
    }
}
