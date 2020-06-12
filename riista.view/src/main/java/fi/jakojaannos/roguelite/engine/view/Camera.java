package fi.jakojaannos.roguelite.engine.view;

@Deprecated
public abstract class Camera implements AutoCloseable {
    public Viewport getViewport() {
        return null;
    }

    public double getPixelsPerUnitX() {
        return getViewport().getWidthInPixels() / getVisibleAreaWidth();
    }

    public double getPixelsPerUnitY() {
        return getViewport().getHeightInPixels() / getVisibleAreaHeight();
    }

    public abstract double getVisibleAreaWidth();

    public abstract double getVisibleAreaHeight();

    public abstract void useWorldCoordinates();

    public abstract void useScreenCoordinates();
}
