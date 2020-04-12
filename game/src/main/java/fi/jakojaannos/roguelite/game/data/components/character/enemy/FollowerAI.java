package fi.jakojaannos.roguelite.game.data.components.character.enemy;

public class FollowerAI {
    public double aggroRadius;
    public double targetDistance;

    public FollowerAI(final double aggroRadius, final double targetDistance) {
        this.aggroRadius = aggroRadius;
        this.targetDistance = targetDistance;
    }
}
