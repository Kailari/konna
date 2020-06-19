package fi.jakojaannos.riista.vulkan.assets.mesh;

import javax.annotation.Nullable;

import fi.jakojaannos.riista.view.assets.Material;
import fi.jakojaannos.riista.view.assets.Mesh;
import fi.jakojaannos.riista.vulkan.assets.material.MaterialImpl;
import fi.jakojaannos.riista.vulkan.internal.GPUBuffer;
import fi.jakojaannos.riista.vulkan.internal.RenderingBackend;
import fi.jakojaannos.riista.vulkan.internal.VertexFormat;
import fi.jakojaannos.riista.vulkan.internal.types.VkMemoryPropertyFlags;

import static fi.jakojaannos.riista.utilities.BitMask.bitMask;
import static org.lwjgl.vulkan.VK10.*;

public class MeshImpl implements Mesh {
    private static final Material DEFAULT_MATERIAL = new MaterialImpl(MaterialImpl.DEFAULT_COLOR,
                                                                      MaterialImpl.DEFAULT_COLOR,
                                                                      MaterialImpl.DEFAULT_COLOR,
                                                                      null,
                                                                      1.0f);

    private final GPUBuffer vertexBuffer;
    private final GPUBuffer indexBuffer;
    private final int indexCount;
    private final Material material;

    public GPUBuffer getVertexBuffer() {
        return this.vertexBuffer;
    }

    public GPUBuffer getIndexBuffer() {
        if (this.indexBuffer == null) {
            throw new IllegalStateException("Tried getting index buffer for non-indexed mesh!");
        }

        return this.indexBuffer;
    }

    @Override
    public int getIndexCount() {
        return this.indexCount;
    }

    @Override
    public Material getMaterial() {
        return this.material;
    }

    public <TVertex> MeshImpl(
            final RenderingBackend backend,
            final VertexFormat<TVertex> vertexFormat,
            final TVertex[] vertices,
            @Nullable final Integer[] indices,
            @Nullable final Material material
    ) {
        this(createVertexBuffer(backend, vertexFormat, vertices),
             createIndexBuffer(backend, indices),
             indices != null ? indices.length : 0,
             material);
    }

    private MeshImpl(
            final GPUBuffer vertexBuffer,
            @Nullable final GPUBuffer indexBuffer,
            final int indexCount,
            @Nullable final Material material
    ) {
        this.vertexBuffer = vertexBuffer;
        this.indexBuffer = indexBuffer;
        this.indexCount = indexCount;

        this.material = material != null
                ? material
                : DEFAULT_MATERIAL;
    }

    @Override
    public void close() {
        this.vertexBuffer.close();

        if (this.indexBuffer != null) {
            this.indexBuffer.close();
        }
    }

    private static <TVertex> GPUBuffer createVertexBuffer(
            final RenderingBackend backend,
            final VertexFormat<TVertex> vertexFormat,
            final TVertex[] vertices
    ) {
        final var buffer = new GPUBuffer(backend.deviceContext(),
                                         vertices.length * vertexFormat.getSizeInBytes(),
                                         VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                                         bitMask(VkMemoryPropertyFlags.DEVICE_LOCAL_BIT));
        buffer.pushWithStagingAndWait(vertices,
                                      vertexFormat.getSizeInBytes(),
                                      vertexFormat::write);

        return buffer;
    }

    @Nullable
    private static GPUBuffer createIndexBuffer(
            final RenderingBackend backend,
            @Nullable final Integer[] indices
    ) {
        if (indices == null) {
            return null;
        }

        final var buffer = new GPUBuffer(backend.deviceContext(),
                                         indices.length * Integer.BYTES,
                                         VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                                         bitMask(VkMemoryPropertyFlags.DEVICE_LOCAL_BIT));
        buffer.pushWithStagingAndWait(indices,
                                      Integer.BYTES,
                                      (value, offset, out) -> out.putInt(offset, value));

        return buffer;
    }
}
