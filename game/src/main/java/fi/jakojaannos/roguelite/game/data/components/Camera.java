package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import org.joml.Vector2d;

public class Camera implements Component {
    public Vector2d pos = new Vector2d();
    public Entity followTarget;
}
