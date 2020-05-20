package fi.jakojaannos.konna.engine.vulkan;

import javax.annotation.Nullable;

import fi.jakojaannos.konna.engine.vulkan.command.CommandBuffer;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.engine.vulkan.types.VkMemoryPropertyFlags;

import static fi.jakojaannos.konna.engine.util.BitMask.bitMask;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class GPUMesh<TVertex> implements AutoCloseable {
    private final GPUBuffer vertexBuffer;
    private final int indexCount;

    @Nullable private final GPUBuffer indexBuffer;

    public int getIndexCount() {
        return this.indexCount;
    }

    public GPUBuffer getIndexBuffer() {
        if (this.indexBuffer == null) {
            throw new IllegalStateException("Tried getting index buffer of non-indexed mesh!");
        }
        return this.indexBuffer;
    }

    public GPUBuffer getVertexBuffer() {
        return this.vertexBuffer;
    }

    public boolean isIndexed() {
        return this.indexBuffer != null && this.indexCount > 0;
    }

    public GPUMesh(
            final DeviceContext deviceContext,
            final VertexFormat<TVertex> vertexFormat,
            final TVertex[] vertices,
            final Integer[] indices
    ) {
        this.indexCount = indices.length;

        this.vertexBuffer = new GPUBuffer(deviceContext,
                                          vertices.length * vertexFormat.getSizeInBytes(),
                                          VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                          bitMask(VkMemoryPropertyFlags.DEVICE_LOCAL_BIT));
        this.vertexBuffer.pushWithStagingAndWait(vertices,
                                                 vertexFormat.getSizeInBytes(),
                                                 vertexFormat::write);

        if (indices.length != 0) {
            this.indexBuffer = new GPUBuffer(deviceContext,
                                             indices.length * Integer.BYTES,
                                             VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                                             bitMask(VkMemoryPropertyFlags.DEVICE_LOCAL_BIT));
            this.indexBuffer.pushWithStagingAndWait(indices,
                                                    Integer.BYTES,
                                                    (index, offset, buffer) -> buffer.putInt(offset, index));
        } else {
            this.indexBuffer = null;
        }
    }

    @Override
    public void close() {
        this.vertexBuffer.close();
        if (this.indexBuffer != null) {
            this.indexBuffer.close();
        }
    }
}
