package fi.jakojaannos.roguelite.game.data.components.character.enemy;

import java.util.Optional;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;

public class AttackAI implements Component {
    public double attackRange;
    @Nullable private Entity attackTarget;

    public AttackAI(final double attackRange) {
        this.attackRange = attackRange;
    }

    public Optional<Entity> getAttackTarget() {
        if (this.attackTarget != null && this.attackTarget.isMarkedForRemoval()) {
            this.attackTarget = null;
        }

        return Optional.ofNullable(this.attackTarget);
    }

    public void setAttackTarget(@Nullable final Entity entity) {
        this.attackTarget = entity;
    }
}
