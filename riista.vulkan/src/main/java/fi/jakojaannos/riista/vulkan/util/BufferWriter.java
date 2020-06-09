package fi.jakojaannos.riista.vulkan.util;

import java.nio.ByteBuffer;

public interface BufferWriter<T> {
    void write(final T value, final int offset, final ByteBuffer buffer);
}
