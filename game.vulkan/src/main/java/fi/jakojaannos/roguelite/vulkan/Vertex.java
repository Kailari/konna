package fi.jakojaannos.roguelite.vulkan;

import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;

public record Vertex(
        Vector3f position,
        Vector3f color
) {
    public static final VertexFormat<Vertex> FORMAT = VertexFormat.<Vertex>builder()
            .attribute(0, VK_FORMAT_R32G32B32_SFLOAT) // position
            .attribute(1, VK_FORMAT_R32G32B32_SFLOAT) // color
            .writer(Vertex::write)
            .build();

    public void write(final int offset, final ByteBuffer buffer) {
        buffer.putFloat(offset, this.position.x);
        buffer.putFloat(offset + 4, this.position.y);
        buffer.putFloat(offset + 8, this.position.z);
        buffer.putFloat(offset + 12, this.color.x);
        buffer.putFloat(offset + 16, this.color.y);
        buffer.putFloat(offset + 20, this.color.z);
    }
}
