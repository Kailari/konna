package fi.jakojaannos.roguelite.game.data.components.character;

import fi.jakojaannos.roguelite.engine.ecs.Component;

public class WeaponInput implements Component {
    public boolean attack;
    public boolean previousAttack;
    public boolean reload;
}
