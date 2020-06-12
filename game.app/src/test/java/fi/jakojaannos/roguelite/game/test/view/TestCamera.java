package fi.jakojaannos.roguelite.game.test.view;

import fi.jakojaannos.roguelite.engine.view.Camera;

@Deprecated
public class TestCamera extends Camera {
    @Override
    public double getVisibleAreaWidth() {
        return 24;
    }

    @Override
    public double getVisibleAreaHeight() {
        return 20;
    }

    @Override
    public void useWorldCoordinates() {

    }

    @Override
    public void useScreenCoordinates() {

    }

    @Override
    public void close() {
    }
}
