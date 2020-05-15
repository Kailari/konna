package fi.jakojaannos.konna.engine.assets.mesh;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import fi.jakojaannos.konna.engine.vulkan.VertexFormat;
import fi.jakojaannos.konna.engine.vulkan.types.VkFormat;

public record StaticMeshVertex(
        Vector3f position,
        Vector3f normal,
        Vector2f textureCoordinates
) {
    public static final VertexFormat<StaticMeshVertex> FORMAT = VertexFormat.<StaticMeshVertex>builder()
            .writer(StaticMeshVertex::write)
            .attribute(0, VkFormat.R32G32B32_SFLOAT)
            .attribute(1, VkFormat.R32G32B32_SFLOAT)
            .attribute(2, VkFormat.R32G32_SFLOAT)
            .build();

    private void write(final int offset, final ByteBuffer buffer) {
        this.position.get(offset, buffer);
        this.normal.get(offset + 12, buffer);
        this.textureCoordinates.get(offset + 24, buffer);
    }
}
