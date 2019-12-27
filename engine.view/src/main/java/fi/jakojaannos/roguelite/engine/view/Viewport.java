package fi.jakojaannos.roguelite.engine.view;

public interface Viewport {
    int getWidthInPixels();

    int getHeightInPixels();

    void resize(int width, int height);
}
