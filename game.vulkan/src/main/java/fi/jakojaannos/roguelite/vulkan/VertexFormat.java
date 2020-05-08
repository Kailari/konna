package fi.jakojaannos.roguelite.vulkan;

import org.lwjgl.system.Struct;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fi.jakojaannos.roguelite.util.BufferWriter;
import fi.jakojaannos.roguelite.vulkan.types.VkFormat;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;

public class VertexFormat<TVertex> implements AutoCloseable {
    private final VkVertexInputBindingDescription.Buffer bindings;
    private final VkVertexInputAttributeDescription.Buffer attributes;
    private final int sizeInBytes;

    private final BufferWriter<TVertex> writer;

    public VkVertexInputBindingDescription.Buffer getBindings() {
        return this.bindings;
    }

    public VkVertexInputAttributeDescription.Buffer getAttributes() {
        return this.attributes;
    }

    public int getSizeInBytes() {
        return this.sizeInBytes;
    }

    private VertexFormat(
            final List<VkVertexInputBindingDescription> bindings,
            final List<VkVertexInputAttributeDescription> attributes,
            final int sizeInBytes,
            final BufferWriter<TVertex> writer
    ) {
        this.bindings = VkVertexInputBindingDescription.calloc(bindings.size());
        for (int i = 0; i < bindings.size(); i++) {
            this.bindings.get(i).set(bindings.get(i));
        }
        this.attributes = VkVertexInputAttributeDescription.calloc(attributes.size());
        for (int i = 0; i < attributes.size(); i++) {
            this.attributes.get(i).set(attributes.get(i));
        }

        this.sizeInBytes = sizeInBytes;
        this.writer = writer;
    }

    public static <TVertex> Builder<TVertex> builder() {
        return new Builder<>();
    }

    public void write(final TVertex vertex, final int offset, final ByteBuffer buffer) {
        this.writer.write(vertex, offset, buffer);
    }

    @Override
    public void close() {
        this.bindings.free();
        this.attributes.free();
    }

    public static class Builder<TVertex> {
        private final List<VkVertexInputAttributeDescription> attributes = new ArrayList<>();

        private BufferWriter<TVertex> writer;
        private int sizeInBytes;

        /**
         * Adds a vertex attribute to the format. Each attribute must define a location and a format. <i>(Both of these
         * must match the attribute location values defined in shaders used with this format)</i>
         *
         * @param location attribute location in the shader
         * @param format   attribute format
         *
         * @return this builder for chaining
         */
        public Builder<TVertex> attribute(
                final int location,
                final VkFormat format
        ) {
            this.attributes.add(VkVertexInputAttributeDescription.calloc()
                                                                 .binding(0)
                                                                 .location(location)
                                                                 .format(format.asInt())
                                                                 .offset(this.sizeInBytes));
            this.sizeInBytes += format.getSize();
            return this;
        }

        /**
         * Adds the vertex writer for this format. The writer is used to serialize the vertices into a {@link
         * ByteBuffer} for uploading to the GPU.
         *
         * @param writer vertex writer
         *
         * @return this builder for chaining
         */
        public Builder<TVertex> writer(final BufferWriter<TVertex> writer) {
            this.writer = writer;
            return this;
        }

        public VertexFormat<TVertex> build() {
            try (final var ignored = stackPush()) {
                final var bindings = VkVertexInputBindingDescription
                        .callocStack()
                        .binding(0)
                        .stride(this.sizeInBytes)
                        .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

                final var format = new VertexFormat<>(List.of(bindings),
                                                      this.attributes,
                                                      this.sizeInBytes,
                                                      Objects.requireNonNull(this.writer));

                this.attributes.forEach(Struct::free);
                return format;
            }
        }
    }
}
