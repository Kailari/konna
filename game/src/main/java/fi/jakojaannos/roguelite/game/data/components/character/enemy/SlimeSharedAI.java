package fi.jakojaannos.roguelite.game.data.components.character.enemy;

import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

import fi.jakojaannos.riista.ecs.EntityHandle;

public class SlimeSharedAI {
    public List<EntityHandle> slimes = new ArrayList<>();

    public boolean regrouping = false;
    public Vector2d regroupPos = new Vector2d();

    public double regroupRadiusSquared = 0.01;

}
