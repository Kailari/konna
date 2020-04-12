package fi.jakojaannos.roguelite.game.data.components.character;

public class WalkingMovementAbility {
    public double maxSpeed;
    public double acceleration;

    public WalkingMovementAbility() {
        this(4.0, 1.0);
    }

    public WalkingMovementAbility(final double maxSpeed, final double acceleration) {
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
    }
}
