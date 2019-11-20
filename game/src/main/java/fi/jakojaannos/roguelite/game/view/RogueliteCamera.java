package fi.jakojaannos.roguelite.game.view;

import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.resources.CameraBounds;
import lombok.val;

class RogueliteCamera extends LWJGLCamera {
    public RogueliteCamera(int viewportWidth, int viewportHeight) {
        super(viewportWidth, viewportHeight);
    }

    void updateConfigurationFromState(GameState state) {
        val camBounds = state.getWorld().getResource(CameraBounds.class);
        refreshTargetScreenSizeInUnits(camBounds.targetViewportSizeInWorldUnits, camBounds.targetViewportSizeRespectiveToMinorAxis);

        // FIXME: THIS BREAKS MVC ENCAPSULATION. Technically, we should queue task on the controller
        //  to make the change as we NEVER should mutate state on the view.
        camBounds.viewportWidthInWorldUnits = getViewportWidthInUnits();
        camBounds.viewportHeightInWorldUnits = getViewportHeightInUnits();
    }
}
