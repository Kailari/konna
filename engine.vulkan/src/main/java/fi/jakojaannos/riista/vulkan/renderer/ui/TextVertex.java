package fi.jakojaannos.riista.vulkan.renderer.ui;

import org.joml.Vector2f;

import java.nio.ByteBuffer;

import fi.jakojaannos.konna.engine.vulkan.VertexFormat;
import fi.jakojaannos.konna.engine.vulkan.types.VkFormat;

public record TextVertex(Vector2f position) {
    private static final int POSITION_OFFSET = 0;

    public static final VertexFormat<TextVertex> FORMAT = VertexFormat.<TextVertex>builder()
            .writer(TextVertex::write)
            .attribute(0, VkFormat.R32G32_SFLOAT)
            .build();


    private void write(final int offset, final ByteBuffer buffer) {
        this.position.get(offset + POSITION_OFFSET, buffer);
    }
}
