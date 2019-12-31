package fi.jakojaannos.roguelite.engine.view;

import org.joml.Vector2d;

public abstract class Camera {
    private final Vector2d position;

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

    public Camera(Vector2d position) {
        this.position = new Vector2d(position);
    }

    public abstract void useWorldCoordinates();

    public abstract void useScreenCoordinates();

    public abstract double getVisibleAreaWidth();

    public abstract double getVisibleAreaHeight();
}
