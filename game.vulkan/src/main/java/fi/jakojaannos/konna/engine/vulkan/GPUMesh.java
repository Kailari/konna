package fi.jakojaannos.konna.engine.vulkan;

import fi.jakojaannos.konna.engine.util.BufferWriter;
import fi.jakojaannos.konna.engine.vulkan.command.CommandBuffer;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.engine.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.konna.engine.vulkan.types.VkMemoryPropertyFlags;

import static fi.jakojaannos.konna.engine.util.BitMask.bitMask;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.*;

public class GPUMesh<TVertex> implements AutoCloseable {
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
        pushToBuffer(this.vertexBuffer,
                     deviceContext,
                     vertices,
                     vertexFormat.getSizeInBytes(),
                     vertexFormat::write);

        this.indexBuffer = new GPUBuffer(deviceContext,
                                         indices.length * Integer.BYTES,
                                         VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                                         bitMask(VkMemoryPropertyFlags.DEVICE_LOCAL_BIT));
        pushToBuffer(this.indexBuffer,
                     deviceContext,
                     indices,
                     Integer.BYTES,
                     (index, offset, buffer) -> buffer.putInt(offset, index));
    }

    public void draw(final CommandBuffer commandBuffer) {
        try (final var stack = stackPush()) {
            vkCmdBindVertexBuffers(commandBuffer.getHandle(),
                                   0,
                                   stack.longs(this.vertexBuffer.getHandle()),
                                   stack.longs(0L));
            vkCmdBindIndexBuffer(commandBuffer.getHandle(),
                                 this.indexBuffer.getHandle(),
                                 0,
                                 VK_INDEX_TYPE_UINT32);

            vkCmdDrawIndexed(commandBuffer.getHandle(),
                             this.indexCount,
                             1,
                             0,
                             0,
                             0);
        }
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

        final var data = memAlloc(dataSizeInBytes);
        try (final var stagingBuffer = new GPUBuffer(
                deviceContext,
                dataSizeInBytes,
                VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                bitMask(VkMemoryPropertyFlags.HOST_VISIBLE_BIT,
                        VkMemoryPropertyFlags.HOST_COHERENT_BIT))
        ) {
            for (int i = 0; i < values.length; ++i) {
                writer.write(values[i], i * elementSize, data);
            }

            stagingBuffer.push(data, 0, dataSizeInBytes);
            stagingBuffer.copyToAndWait(commandPool, deviceContext.getTransferQueue(), buffer);
        } finally {
            memFree(data);
        }
    }
}
