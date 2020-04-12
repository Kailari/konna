package fi.jakojaannos.roguelite.game.data.components.character.enemy;

public class StalkerAI {
    public long lastJumpTimeStamp = -1000; // TODO: Move to LeapAbility
    public double aggroRadiusSquared;
    public double leapRadiusSquared;
    public long jumpCoolDownInTicks;
    public long jumpDurationInTicks;
    public double moveSpeedSneak;
    public double moveSpeedWalk;
    public double moveSpeedJump;

    public StalkerAI() {
        this(750.0,
             50.0,
             200,
             20,
             1.5,
             4.5,
             14.0);
    }

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
}
