package fi.jakojaannos.konna.engine.assets;

import javax.annotation.Nullable;

import fi.jakojaannos.konna.engine.assets.mesh.MeshImpl;
import fi.jakojaannos.konna.engine.vulkan.GPUBuffer;
import fi.jakojaannos.konna.engine.vulkan.RenderingBackend;
import fi.jakojaannos.konna.engine.vulkan.VertexFormat;

public interface Mesh extends AutoCloseable {
    GPUBuffer getVertexBuffer();

    GPUBuffer getIndexBuffer();

    int getIndexCount();

    Material getMaterial();

    static <TVertex> Mesh from(
            final RenderingBackend backend,
            final VertexFormat<TVertex> vertexFormat,
            final TVertex[] vertices,
            @Nullable final Integer[] indices,
            @Nullable final Material material
    ) {
        return new MeshImpl(backend, vertexFormat, vertices, indices, material);
    }

    static <TVertex> Mesh from(
            final RenderingBackend backend,
            final VertexFormat<TVertex> vertexFormat,
            final TVertex[] vertices
    ) {
        return new MeshImpl(backend, vertexFormat, vertices, null, null);
    }

    @Override
    void close();
}
