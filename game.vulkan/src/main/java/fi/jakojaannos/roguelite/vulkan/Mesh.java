package fi.jakojaannos.roguelite.vulkan;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class Mesh implements AutoCloseable {
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
            final Vertex[] vertices,
            final Short[] indices
    ) {
        final var vertexFormat = Vertex.FORMAT;
        this.indexCount = indices.length;

        this.vertexBuffer = new GPUBuffer(deviceContext,
                                          vertices.length * vertexFormat.getSizeInBytes(),
                                          VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                          VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        pushToBuffer(this.vertexBuffer,
                     deviceContext,
                     vertices,
                     vertexFormat.getSizeInBytes(),
                     (buffer, vertex) -> vertex.write(buffer));

        this.indexBuffer = new GPUBuffer(deviceContext,
                                         indices.length * Short.BYTES,
                                         VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                                         VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        pushToBuffer(this.indexBuffer,
                     deviceContext,
                     indices,
                     Short.BYTES,
                     ByteBuffer::putShort);
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
            final BiConsumer<ByteBuffer, T> writer
    ) {
        final var commandPool = deviceContext.getTransferCommandPool();
        final var dataSizeInBytes = values.length * elementSize;

        try (final var stack = stackPush();
             final var stagingBuffer = new GPUBuffer(
                     deviceContext,
                     dataSizeInBytes,
                     VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                     VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
        ) {
            final var data = stack.malloc(dataSizeInBytes);
            for (final var value : values) {
                writer.accept(data, value);
            }
            data.flip();

            stagingBuffer.push(data, 0, dataSizeInBytes);
            stagingBuffer.copyToAndWait(commandPool, deviceContext.getTransferQueue(), buffer);
        }
    }
}
