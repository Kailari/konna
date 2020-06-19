package fi.jakojaannos.roguelite.game.data.components.character.enemy;

import java.util.Optional;
import javax.annotation.Nullable;

import fi.jakojaannos.riista.ecs.EntityHandle;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;

public class AttackAI {
    public final Class<?> targetTagClass;
    public double attackRange;
    @Nullable private EntityHandle attackTarget;

    public Optional<EntityHandle> getAttackTarget() {
        if (this.attackTarget != null && this.attackTarget.isPendingRemoval()) {
            this.attackTarget = null;
        }

        return Optional.ofNullable(this.attackTarget);
    }

    public void setAttackTarget(@Nullable final EntityHandle entity) {
        this.attackTarget = entity;
    }

    public AttackAI(final double attackRange) {
        this(PlayerTag.class, attackRange);
    }

    public AttackAI(final Class<?> targetTagClass, final double attackRange) {
        this.targetTagClass = targetTagClass;
        this.attackRange = attackRange;
    }

    public void clearAttackTarget() {
        this.attackTarget = null;
    }
}
