package fi.jakojaannos.roguelite.game.data.components.character.enemy;

import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

import fi.jakojaannos.riista.ecs.legacy.Entity;

public class SlimeSharedAI {

    public List<Entity> slimes = new ArrayList<>();

    public boolean regrouping = false;
    public Vector2d regroupPos = new Vector2d();

    public double regroupRadiusSquared = 0.01;

}
