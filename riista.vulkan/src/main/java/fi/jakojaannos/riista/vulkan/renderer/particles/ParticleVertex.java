package fi.jakojaannos.riista.vulkan.renderer.particles;

import java.nio.ByteBuffer;

import fi.jakojaannos.riista.vulkan.internal.VertexFormat;

public record ParticleVertex() {
    public static final VertexFormat<ParticleVertex> FORMAT = VertexFormat.<ParticleVertex>builder()
            .writer(ParticleVertex::write)
            .build();

    private void write(final int offset, final ByteBuffer buffer) {
    }
}
