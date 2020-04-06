package fi.jakojaannos.roguelite.game.data.components.character.enemy;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Component;

public class FollowerAI implements Component {
    public double aggroRadius;
    public double targetDistance;

    public FollowerAI(final double aggroRadius, final double targetDistance) {
        this.aggroRadius = aggroRadius;
        this.targetDistance = targetDistance;
    }
}
