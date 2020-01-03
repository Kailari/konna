package fi.jakojaannos.roguelite.game.data.resources;

import fi.jakojaannos.roguelite.engine.ecs.Resource;

public class Inputs implements Resource {
    public boolean inputLeft = false;
    public boolean inputRight = false;
    public boolean inputUp = false;
    public boolean inputDown = false;
    public boolean inputAttack = false;

    public boolean inputRestart = false;
    public boolean inputMenu = false;

    public boolean inputForceCloseA;
    public boolean inputForceCloseB;
}
