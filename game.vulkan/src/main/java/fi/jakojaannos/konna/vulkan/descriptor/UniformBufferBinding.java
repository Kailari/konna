package fi.jakojaannos.konna.vulkan.descriptor;

import java.nio.ByteBuffer;

public interface UniformBufferBinding {
    int binding();

    long sizeInBytes();

    void write(int offset, ByteBuffer buffer);
}
