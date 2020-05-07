package fi.jakojaannos.roguelite.vulkan;

import fi.jakojaannos.roguelite.util.BufferWriter;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.types.VkMemoryPropertyFlags;

import static fi.jakojaannos.roguelite.util.BitMask.bitMask;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class Mesh<TVertex> implements AutoCloseable {
    private final GPUBuffer vertexBuffer;
    private final GPUBuffer indexBuffer;
    private final int indexCount;

    public int getIndexCount() {
        return this.indexCount;
    }

    public GPUBuffer getIndexBuffer() {
        return this.indexBuffer;
    }

    public GPUBuffer getVertexBuffer() {
        return this.vertexBuffer;
    }

    public Mesh(
            final DeviceContext deviceContext,
            final VertexFormat<TVertex> vertexFormat,
            final TVertex[] vertices,
            final Short[] indices
    ) {
        this.indexCount = indices.length;

        this.vertexBuffer = new GPUBuffer(deviceContext,
                                          vertices.length * vertexFormat.getSizeInBytes(),
                                          VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                          bitMask(VkMemoryPropertyFlags.DEVICE_LOCAL_BIT));
        pushToBuffer(this.vertexBuffer,
                     deviceContext,
                     vertices,
                     vertexFormat.getSizeInBytes(),
                     vertexFormat::write);

        this.indexBuffer = new GPUBuffer(deviceContext,
                                         indices.length * Short.BYTES,
                                         VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                                         bitMask(VkMemoryPropertyFlags.DEVICE_LOCAL_BIT));
        pushToBuffer(this.indexBuffer,
                     deviceContext,
                     indices,
                     Short.BYTES,
                     (index, offset, buffer) -> buffer.putShort(offset, index));
    }

    @Override
    public void close() {
        this.vertexBuffer.close();
        this.indexBuffer.close();
    }

    private static <T> void pushToBuffer(
            final GPUBuffer buffer,
            final DeviceContext deviceContext,
            final T[] values,
            final int elementSize,
            final BufferWriter<T> writer
    ) {
        final var commandPool = deviceContext.getTransferCommandPool();
        final var dataSizeInBytes = values.length * elementSize;

        try (final var stack = stackPush();
             final var stagingBuffer = new GPUBuffer(
                     deviceContext,
                     dataSizeInBytes,
                     VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                     bitMask(VkMemoryPropertyFlags.HOST_VISIBLE_BIT,
                             VkMemoryPropertyFlags.HOST_COHERENT_BIT))
        ) {
            final var data = stack.calloc(dataSizeInBytes);
            for (int i = 0; i < values.length; ++i) {
                writer.write(values[i], i * elementSize, data);
            }

            stagingBuffer.push(data, 0, dataSizeInBytes);
            stagingBuffer.copyToAndWait(commandPool, deviceContext.getTransferQueue(), buffer);
        }
    }
}
