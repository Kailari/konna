package fi.jakojaannos.roguelite.engine.view;

public class Viewport {
    private int width;
    private int height;

    public Viewport(final int width, final int height) {
        resize(width, height);
    }

    public int getWidthInPixels() {
        return this.width;
    }

    public int getHeightInPixels() {
        return this.height;
    }

    public void resize(final int width, final int height) {
        this.width = width;
        this.height = height;
    }
}
