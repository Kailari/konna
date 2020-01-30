package fi.jakojaannos.roguelite.game.data.components.character.enemy;

import lombok.NoArgsConstructor;

import fi.jakojaannos.roguelite.engine.ecs.Component;

@NoArgsConstructor
public class StalkerAI implements Component {
    public double aggroRadiusSquared = 750.0;
    public double leapRadiusSquared = 50.0;
    public long lastJumpTimeStamp = -1000;
    public long jumpCoolDownInTicks = 200;
    public long jumpDurationInTicks = 20;
    public double moveSpeedSneak = 1.5;
    public double moveSpeedWalk = 4.5;
    public double moveSpeedJump = 14.0;

    public StalkerAI(
            final double aggroRadiusSquared,
            final double leapRadiusSquared,
            final long jumpCoolDownInTicks,
            final long jumpDurationInTicks,
            final double moveSpeedSneak,
            final double moveSpeedWalk,
            final double moveSpeedJump
    ) {
        this.aggroRadiusSquared = aggroRadiusSquared;
        this.leapRadiusSquared = leapRadiusSquared;
        this.jumpCoolDownInTicks = jumpCoolDownInTicks;
        this.jumpDurationInTicks = jumpDurationInTicks;
        this.moveSpeedSneak = moveSpeedSneak;
        this.moveSpeedWalk = moveSpeedWalk;
        this.moveSpeedJump = moveSpeedJump;
    }

    public StalkerAI(
            final double aggroRadiusSquared,
            final double leapRadiusSquared,
            final double moveSpeedSneak,
            final double moveSpeedWalk
    ) {
        this.aggroRadiusSquared = aggroRadiusSquared;
        this.leapRadiusSquared = leapRadiusSquared;
        this.moveSpeedSneak = moveSpeedSneak;
        this.moveSpeedWalk = moveSpeedWalk;
    }
}
