package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.mesh;

import java.util.ArrayList;
import java.util.List;

import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexAttribute;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormat;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormatBuilder;

public class LWJGLVertexFormatBuilder implements VertexFormatBuilder {
    private List<VertexAttribute> attributes = new ArrayList<>();

    @Override
    public VertexFormatBuilder withAttribute(
            final VertexAttribute.Type type,
            final int count,
            final boolean normalized
    ) {
        this.attributes.add(new LWJGLVertexAttribute(type, count, normalized));
        return this;
    }

    @Override
    public VertexFormat build() {
        return new VertexFormat(this.attributes.toArray(new VertexAttribute[0]));
    }
}
