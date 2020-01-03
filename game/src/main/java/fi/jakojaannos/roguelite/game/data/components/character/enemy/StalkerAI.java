package fi.jakojaannos.roguelite.game.data.components.character.enemy;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import org.joml.Vector2d;

public class StalkerAI implements Component {

    public double sneakRadiusSquared, leapRadiusSquared, jumpAbilityGoesCoolDownThisLong;
    public double jumpCoolDown, airTime;
    public Vector2d jumpDir;

    public StalkerAI(double sneakRadiusSquared, double leapRadiusSquared, double jumpCoolDownTimer) {
        this.sneakRadiusSquared = sneakRadiusSquared;
        this.leapRadiusSquared = leapRadiusSquared;
        this.jumpAbilityGoesCoolDownThisLong = jumpCoolDownTimer;
        jumpDir = new Vector2d(0.0f, 0.0f);

        this.jumpCoolDown = 0;
        this.airTime = 0;
    }
}
