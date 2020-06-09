package fi.jakojaannos.riista.vulkan.internal.memory;

import java.nio.ByteBuffer;

public interface GPUMemory extends AutoCloseable {
    /**
     * Binds the given buffer to this memory with the given offset.
     *
     * @param handle buffer handle
     * @param offset memory offset for the start of the buffer
     */
    void bindBuffer(final long handle, final long offset);

    /**
     * Binds the given image to this memory with the given offset.
     *
     * @param handle image handle
     * @param offset memory offset for the start of the buffer
     */
    void bindImage(final long handle, final long offset);

    /**
     * Pushes the given data to this memory with the given offset.
     *
     * @param data   data to push
     * @param offset offset from the start of this memory region
     * @param size   number of bytes to read from the buffer and write into this memory region
     */
    void push(final ByteBuffer data, final long offset, final long size);

    // Override to remove the `throws Exception`
    @Override
    void close();
}
