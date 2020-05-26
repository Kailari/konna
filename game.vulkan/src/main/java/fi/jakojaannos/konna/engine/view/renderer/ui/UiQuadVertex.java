package fi.jakojaannos.konna.engine.view.renderer.ui;

import org.joml.Vector2f;

import java.nio.ByteBuffer;

import fi.jakojaannos.konna.engine.vulkan.VertexFormat;
import fi.jakojaannos.konna.engine.vulkan.types.VkFormat;

public record UiQuadVertex(Vector2f position) {
    public static final VertexFormat<UiQuadVertex> FORMAT = VertexFormat.<UiQuadVertex>builder()
            .writer(UiQuadVertex::write)
            .attribute(0, VkFormat.R32G32_SFLOAT)
            .build();

    private void write(final int offset, final ByteBuffer buffer) {
        this.position.get(offset, buffer);
    }
}
