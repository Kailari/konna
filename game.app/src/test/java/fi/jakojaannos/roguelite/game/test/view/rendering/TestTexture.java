package fi.jakojaannos.roguelite.game.test.view.rendering;

import lombok.Getter;

import java.awt.image.BufferedImage;

import fi.jakojaannos.roguelite.engine.view.rendering.Texture;

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
