package fi.jakojaannos.roguelite.game.test.view.rendering;

import java.awt.image.BufferedImage;

import fi.jakojaannos.roguelite.engine.view.rendering.Texture;

public class TestTexture implements Texture {
    private final int width;
    private final int height;

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

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
