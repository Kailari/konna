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

    public void write(final ByteBuffer buffer) {
        buffer.putFloat(this.position.x);
        buffer.putFloat(this.position.y);
        buffer.putFloat(this.position.z);
        buffer.putFloat(this.color.x);
        buffer.putFloat(this.color.y);
        buffer.putFloat(this.color.z);
    }
}
