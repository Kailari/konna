package fi.jakojaannos.roguelite.engine.view;

import fi.jakojaannos.roguelite.engine.state.GameState;
import lombok.Getter;
import org.joml.Vector2d;

// TODO: Move anything that can be abstracted from LWJGLCamera here (allows better testing matrices etc. producing correct coordinates)

public abstract class Camera implements AutoCloseable {
    private final Vector2d position;
    @Getter private final Viewport viewport;

    public final double getX() {
        return this.position.x;
    }

    public final double getY() {
        return this.position.y;
    }

    public final void setX(double x) {
        setPosition(x, getY());
    }

    public final void setY(double y) {
        setPosition(getX(), y);
    }

    public void setPosition(double x, double y) {
        this.position.set(x, y);
    }

    public Camera(final Vector2d position, final Viewport viewport) {
        this.position = new Vector2d(position);
        this.viewport = viewport;
    }

    public abstract void resize(int width, int height);

    public double getPixelsPerUnitX() {
        return getViewport().getWidthInPixels() / getVisibleAreaWidth();
    }

    public double getPixelsPerUnitY() {
        return getViewport().getHeightInPixels() / getVisibleAreaHeight();
    }

    public abstract void useWorldCoordinates();

    public abstract void useScreenCoordinates();

    public abstract double getVisibleAreaWidth();

    public abstract double getVisibleAreaHeight();

    public abstract void updateConfigurationFromState(GameState state);
}
