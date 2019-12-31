package fi.jakojaannos.roguelite.engine.data.resources;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.Resource;

public class CameraProperties implements Resource {
    public double viewportWidthInWorldUnits;
    public double viewportHeightInWorldUnits;

    public double targetViewportSizeInWorldUnits = 24.0;
    public boolean targetViewportSizeRespectiveToMinorAxis = true;

    public Entity cameraEntity;
}
