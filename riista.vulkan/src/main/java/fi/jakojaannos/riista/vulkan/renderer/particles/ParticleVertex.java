package fi.jakojaannos.riista.vulkan.renderer.particles;

import org.joml.Vector3f;

import java.nio.ByteBuffer;

import fi.jakojaannos.riista.vulkan.internal.VertexFormat;
import fi.jakojaannos.riista.vulkan.internal.types.VkFormat;

public record ParticleVertex(
        Vector3f position
) {
    public static final VertexFormat<ParticleVertex> FORMAT = VertexFormat.<ParticleVertex>builder()
            .writer(ParticleVertex::write)
            .attribute(0, VkFormat.R32G32B32_SFLOAT)
            .build();

    private void write(final int offset, final ByteBuffer buffer) {
        this.position.get(offset, buffer);
    }
}
