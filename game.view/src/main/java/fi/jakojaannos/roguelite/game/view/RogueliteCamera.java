package fi.jakojaannos.roguelite.game.view;

import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.game.data.resources.CameraProperties;
import lombok.val;

class RogueliteCamera extends LWJGLCamera {
    public RogueliteCamera(int viewportWidth, int viewportHeight) {
        super(viewportWidth, viewportHeight);
    }

    void updateConfigurationFromState(GameState state) {
        // Ensure matrices are updated
        getProjectionMatrix();
        getViewMatrix();

        val camBounds = state.getWorld().getOrCreateResource(CameraProperties.class);
        refreshTargetScreenSizeInUnits(camBounds.targetViewportSizeInWorldUnits, camBounds.targetViewportSizeRespectiveToMinorAxis);

        // FIXME: THIS BREAKS MVC ENCAPSULATION. Technically, we should queue task on the controller
        //  to make the change as we NEVER should mutate state on the view.
        camBounds.viewportWidthInWorldUnits = getViewportWidthInUnits();
        camBounds.viewportHeightInWorldUnits = getViewportHeightInUnits();
    }
}
