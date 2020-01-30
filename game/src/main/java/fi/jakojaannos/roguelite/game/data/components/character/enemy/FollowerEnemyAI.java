package fi.jakojaannos.roguelite.game.data.components.character.enemy;

import fi.jakojaannos.roguelite.engine.ecs.Component;

public class FollowerEnemyAI implements Component {
    public double aggroRadius;
    public double targetDistance;

    public FollowerEnemyAI(final double aggroRadius, final double targetDistance) {
        this.aggroRadius = aggroRadius;
        this.targetDistance = targetDistance;
    }
}
