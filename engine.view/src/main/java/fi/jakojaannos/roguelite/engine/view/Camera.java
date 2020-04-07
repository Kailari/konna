package fi.jakojaannos.roguelite.engine.view;

import org.joml.Vector2d;

import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.state.GameState;

// TODO: Move anything that can be abstracted from LWJGLCamera here
//  (allows better testing matrices etc. producing correct coordinates)

public abstract class Camera implements AutoCloseable {
    private final Vector2d position;
    private final Viewport viewport;

    public Viewport getViewport() {
        return this.viewport;
    }

    public final double getX() {
        return this.position.x;
    }

    public final void setX(final double x) {
        setPosition(x, getY());
    }

    public final double getY() {
        return this.position.y;
    }

    public final void setY(final double y) {
        setPosition(getX(), y);
    }

    public double getPixelsPerUnitX() {
        return getViewport().getWidthInPixels() / getVisibleAreaWidth();
    }

    public double getPixelsPerUnitY() {
        return getViewport().getHeightInPixels() / getVisibleAreaHeight();
    }

    public abstract double getVisibleAreaWidth();

    public abstract double getVisibleAreaHeight();

    public Camera(final Vector2d position, final Viewport viewport) {
        this.position = new Vector2d(position);
        this.viewport = viewport;
    }

    public void setPosition(final double x, final double y) {
        this.position.set(x, y);
    }

    public abstract void resize(int width, int height);

    public abstract void useWorldCoordinates();

    public abstract void useScreenCoordinates();

    public void updateConfigurationFromState(final GameState state) {
        final var world = state.getWorld();
        final var cameraProperties = world.fetchResource(CameraProperties.class);

        // FIXME: THIS BREAKS MVC ENCAPSULATION. Technically, we should queue task on the controller
        //  to make the change as we NEVER should mutate state on the view.
        cameraProperties.viewportWidthInWorldUnits = getVisibleAreaWidth();
        cameraProperties.viewportHeightInWorldUnits = getVisibleAreaHeight();
    }
}
