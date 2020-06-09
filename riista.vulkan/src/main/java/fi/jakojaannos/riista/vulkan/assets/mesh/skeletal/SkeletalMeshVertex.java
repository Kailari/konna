package fi.jakojaannos.riista.vulkan.assets.mesh.skeletal;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4i;

import java.nio.ByteBuffer;

import fi.jakojaannos.riista.vulkan.internal.VertexFormat;
import fi.jakojaannos.riista.vulkan.internal.types.VkFormat;

public record SkeletalMeshVertex(
        Vector3f position,
        Vector3f normal,
        Vector2f textureCoordinates,
        Vector4f boneWeights,
        Vector4i boneIds
) {
    public static final VertexFormat<SkeletalMeshVertex> FORMAT = VertexFormat.<SkeletalMeshVertex>builder()
            .writer(SkeletalMeshVertex::write)
            .attribute(0, VkFormat.R32G32B32_SFLOAT)
            .attribute(1, VkFormat.R32G32B32_SFLOAT)
            .attribute(2, VkFormat.R32G32_SFLOAT)
            .attribute(3, VkFormat.R32G32B32A32_SFLOAT)
            .attribute(4, VkFormat.R8G8B8A8_UINT)
            .build();

    private void write(final int offset, final ByteBuffer buffer) {
        this.position.get(offset, buffer);
        this.normal.get(offset + 12, buffer);
        this.textureCoordinates.get(offset + 24, buffer);

        this.boneWeights.get(offset + 32, buffer);
        buffer.put(offset + 48, (byte) (this.boneIds.x() & 0xFF));
        buffer.put(offset + 49, (byte) (this.boneIds.y() & 0xFF));
        buffer.put(offset + 50, (byte) (this.boneIds.z() & 0xFF));
        buffer.put(offset + 51, (byte) (this.boneIds.w() & 0xFF));
    }
}
