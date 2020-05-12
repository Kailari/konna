package fi.jakojaannos.roguelite.vulkan.uniform;

import java.nio.ByteBuffer;

public interface UniformBufferBinding {
    int binding();

    long sizeInBytes();

    void write(int offset, ByteBuffer buffer);
}
