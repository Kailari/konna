package fi.jakojaannos.konna.engine.view.renderer.ui;

import org.joml.Vector2f;

import java.nio.ByteBuffer;

import fi.jakojaannos.konna.engine.vulkan.VertexFormat;
import fi.jakojaannos.konna.engine.vulkan.types.VkFormat;

public record TextVertex(Vector2f position, Vector2f uv, int uvIndex) {
    private static final int POSITION_OFFSET = 0;
    private static final int UV_OFFSET = 2 * Float.BYTES;
    private static final int UV_INDEX_OFFSET = 4 * Float.BYTES;

    public static final VertexFormat<TextVertex> FORMAT = VertexFormat.<TextVertex>builder()
            .writer(TextVertex::write)
            .attribute(0, VkFormat.R32G32_SFLOAT)
            .attribute(2, VkFormat.R32_UINT)
            .build();


    private void write(final int offset, final ByteBuffer buffer) {
        this.position.get(offset + POSITION_OFFSET, buffer);
        this.uv.get(offset + UV_OFFSET, buffer);
        buffer.putInt(UV_INDEX_OFFSET, this.uvIndex);
    }
}
