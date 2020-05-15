package fi.jakojaannos.konna.engine.vulkan.descriptor;

import java.nio.ByteBuffer;

public interface UniformBufferBinding {
    int binding();

    long sizeInBytes();

    void write(int offset, ByteBuffer buffer);
}
