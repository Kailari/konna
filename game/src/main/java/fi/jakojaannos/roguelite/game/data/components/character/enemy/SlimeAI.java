package fi.jakojaannos.roguelite.game.data.components.character.enemy;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import org.joml.Vector2d;

public class SlimeAI implements Component {

    public double chaseRadiusSquared = 100.0 * 100.0,
            targetRadiusSquared = 1.0;

    public double crawlSpeed = 0.5;

    public long lastJumpTimeStamp = -100, jumpCoolDownInTicks = 50, jumpDurationInTicks = 30;
    public double jumpForce = 5.0;

    public double slimeSize = 3;
    public int offspringAmountAfterDeath = 4;

    public SlimeAI() {
    }

}
