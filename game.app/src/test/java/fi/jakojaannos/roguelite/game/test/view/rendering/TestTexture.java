package fi.jakojaannos.roguelite.game.test.view.rendering;

import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import lombok.Getter;

import java.awt.image.BufferedImage;

public class TestTexture implements Texture {
    @Getter private final int width;
    @Getter private final int height;

    public TestTexture(final int width, final int height, final BufferedImage imageData) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void use() {
    }

    @Override
    public void close() {
    }
}
