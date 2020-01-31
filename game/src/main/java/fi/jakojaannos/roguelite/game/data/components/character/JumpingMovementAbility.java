package fi.jakojaannos.roguelite.game.data.components.character;

import fi.jakojaannos.roguelite.engine.ecs.Component;

public class JumpingMovementAbility implements Component {
    public long jumpCoolDownInTicks = 50;
    public long jumpDurationInTicks = 30;
    public double jumpForce = 5.0;

    public long lastJumpTimeStamp = -100;
}
