package fi.jakojaannos.roguelite.util;

import java.nio.ByteBuffer;

public interface BufferWriter<T> {
    void write(final T value, final int offset, final ByteBuffer buffer);
}
