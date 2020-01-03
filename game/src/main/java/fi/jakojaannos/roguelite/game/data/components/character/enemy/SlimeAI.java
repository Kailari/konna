package fi.jakojaannos.roguelite.game.data.components.character.enemy;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import org.joml.Vector2d;

public class SlimeAI implements Component {

    public double chaseRadiusSquared = 100.0 * 100.0,
            targetRadiusSquared = 1.0;

    public double crawlSpeed = 0.5;

    public double airTime = 0.0, setAirTimeCoolDown = 0.6,
            jumpCoolDown = 0.0, setJumpCoolDown = 1.0,
            regroupTimer = 5.0;

    public int slimeSize = 3;

    public final Vector2d jumpDir = new Vector2d();

    public SlimeAI() {
    }

    public SlimeAI(
            double chaseRadiusSquared,
            double targetRadiusSquared,
            double airTimeCoolDown,
            double jumpCoolDown,
            int slimeSize
    ) {
        this.chaseRadiusSquared = chaseRadiusSquared;
        this.targetRadiusSquared = targetRadiusSquared;
        this.setAirTimeCoolDown = airTimeCoolDown;
        this.setJumpCoolDown = jumpCoolDown;
        this.slimeSize = slimeSize;
    }

    public SlimeAI(double airTimeCoolDown, double jumpCoolDown, int slimeSize) {
        this.setAirTimeCoolDown = airTimeCoolDown;
        this.setJumpCoolDown = jumpCoolDown;
        this.slimeSize = slimeSize;
    }


}
