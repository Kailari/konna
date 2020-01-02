package fi.jakojaannos.roguelite.engine.view;

import fi.jakojaannos.roguelite.engine.ui.UserInterface;

public class Viewport implements UserInterface.ViewportSizeProvider {
    private int width;
    private int height;

    public Viewport(final int width, final int height) {
        resize(width, height);
    }

    @Override
    public int getWidthInPixels() {
        return this.width;
    }

    @Override
    public int getHeightInPixels() {
        return this.height;
    }

    public void resize(final int width, final int height) {
        this.width = width;
        this.height = height;
    }
}
