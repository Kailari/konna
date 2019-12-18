package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class SlimeSharedAI implements Component {

    public List<Entity> slimes = new ArrayList<>();

    public boolean regrouping = false;
    public Vector2d regroupPos = new Vector2d();


}
