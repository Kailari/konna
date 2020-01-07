package fi.jakojaannos.roguelite.game.test.view;

import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import org.joml.Vector2d;

public class TestCamera extends Camera {
    public TestCamera(final Viewport viewport) {
        super(new Vector2d(0), viewport);
    }

    @Override
    public void resize(final int width, final int height) {

    }

    @Override
    public void useWorldCoordinates() {

    }

    @Override
    public void useScreenCoordinates() {

    }

    @Override
    public double getVisibleAreaWidth() {
        return 24;
    }

    @Override
    public double getVisibleAreaHeight() {
        return 20;
    }

    @Override
    public void close() {
    }
}
