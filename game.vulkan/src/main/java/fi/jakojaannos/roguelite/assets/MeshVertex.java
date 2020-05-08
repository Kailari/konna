package fi.jakojaannos.roguelite.assets;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import fi.jakojaannos.roguelite.vulkan.VertexFormat;
import fi.jakojaannos.roguelite.vulkan.types.VkFormat;

public record MeshVertex(
        Vector3f position,
        Vector3f normal,
        Vector2f textureCoordinates
) {
    public static final VertexFormat<MeshVertex> FORMAT = VertexFormat.<MeshVertex>builder()
            .writer(MeshVertex::write)
            .attribute(0, VkFormat.R32G32B32_SFLOAT)
            .attribute(1, VkFormat.R32G32B32_SFLOAT)
            .attribute(2, VkFormat.R32G32_SFLOAT)
            .build();

    private void write(final int offset, final ByteBuffer buffer) {
        buffer.putFloat(offset, this.position.x());
        buffer.putFloat(offset + 4, this.position.y());
        buffer.putFloat(offset + 8, this.position.z());

        buffer.putFloat(offset + 12, this.normal.x());
        buffer.putFloat(offset + 16, this.normal.y());
        buffer.putFloat(offset + 20, this.normal.z());

        buffer.putFloat(offset + 24, this.textureCoordinates.x());
        buffer.putFloat(offset + 28, this.textureCoordinates.y());
    }
}
