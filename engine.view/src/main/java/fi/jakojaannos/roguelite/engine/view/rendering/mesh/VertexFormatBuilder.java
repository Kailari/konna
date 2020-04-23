package fi.jakojaannos.roguelite.engine.view.rendering.mesh;

public interface VertexFormatBuilder {
    VertexFormatBuilder withAttribute(VertexAttribute.Type type, int count, boolean normalized);

    VertexFormatBuilder withInstanceAttribute(VertexAttribute.Type type, int count, boolean normalized);

    VertexFormat build();
}
