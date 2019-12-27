package fi.jakojaannos.roguelite.game.view;

import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.game.data.resources.CameraProperties;
import lombok.val;

class RogueliteCamera extends LWJGLCamera {
    public RogueliteCamera(final Viewport viewport) {
        super(viewport);
    }

    void updateConfigurationFromState(GameState state) {
        val camBounds = state.getWorld().getOrCreateResource(CameraProperties.class);
        refreshTargetScreenSizeInUnits(camBounds.targetViewportSizeInWorldUnits, camBounds.targetViewportSizeRespectiveToMinorAxis);

        // FIXME: THIS BREAKS MVC ENCAPSULATION. Technically, we should queue task on the controller
        //  to make the change as we NEVER should mutate state on the view.
        camBounds.viewportWidthInWorldUnits = getVisibleAreaWidth();
        camBounds.viewportHeightInWorldUnits = getVisibleAreaHeight();
    }
}
