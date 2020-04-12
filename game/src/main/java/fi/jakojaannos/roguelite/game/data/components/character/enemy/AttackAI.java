package fi.jakojaannos.roguelite.game.data.components.character.enemy;

import java.util.Optional;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;

public class AttackAI {
    public final Class<?> targetTagClass;
    public double attackRange;
    @Nullable private Entity attackTarget;

    public AttackAI(final double attackRange) {
        this(PlayerTag.class, attackRange);
    }

    public AttackAI(final Class<?> targetTagClass, final double attackRange) {
        this.targetTagClass = targetTagClass;
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

    public void clearAttackTarget() {
        this.attackTarget = null;
    }
}
