package fi.jakojaannos.roguelite.vulkan.uniform;

import java.nio.ByteBuffer;

import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorBinding;

public interface UniformBinding {
    long getSizeInBytes();

    DescriptorBinding getDescriptorBinding();

    void write(int offset, ByteBuffer buffer);
}
