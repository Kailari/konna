package fi.jakojaannos.roguelite.game.data.components.character;

import org.joml.Vector2d;

import fi.jakojaannos.roguelite.engine.ecs.Component;

public class CharacterInput implements Component {
    public Vector2d move = new Vector2d(0.0f, 0.0f);

    public boolean attack = false;
}
