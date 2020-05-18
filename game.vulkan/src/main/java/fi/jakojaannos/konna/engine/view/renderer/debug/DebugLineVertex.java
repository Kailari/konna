package fi.jakojaannos.konna.engine.view.renderer.debug;

import org.joml.Vector3f;

import java.nio.ByteBuffer;

import fi.jakojaannos.konna.engine.vulkan.VertexFormat;
import fi.jakojaannos.konna.engine.vulkan.types.VkFormat;

public record DebugLineVertex(
        Vector3f position,
        Vector3f color
) {
    public static final VertexFormat<DebugLineVertex> FORMAT = VertexFormat.<DebugLineVertex>builder()
            .writer(DebugLineVertex::write)
            .attribute(0, VkFormat.R32G32B32_SFLOAT)
            .attribute(1, VkFormat.R32G32B32_SFLOAT)
            .build();

    private void write(final int offset, final ByteBuffer buffer) {
        this.position.get(offset, buffer);
        this.color.get(offset + 3 * Float.BYTES, buffer);
    }
}
