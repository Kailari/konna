package fi.jakojaannos.roguelite.game.data.components.character.enemy;

import fi.jakojaannos.roguelite.engine.ecs.Component;

public class FollowerEnemyAI implements Component {

    public double aggroRadius, targetDistance;

    public FollowerEnemyAI(double aggroRadius, double targetDistance) {
        this.aggroRadius = aggroRadius;
        this.targetDistance = targetDistance;
    }

}
