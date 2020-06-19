package fi.jakojaannos.roguelite.game.systems.characters.ai;

import org.joml.Vector2d;

import java.util.Optional;
import java.util.stream.Stream;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.ecs.EntityHandle;
import fi.jakojaannos.riista.ecs.resources.Entities;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.AttackAI;
import fi.jakojaannos.roguelite.game.data.resources.Players;

public class AttackAIControllerSystem implements EcsSystem<AttackAIControllerSystem.Resources, AttackAIControllerSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var maybeLocalPlayer = resources.players.getLocalPlayer();
        entities.forEach(entity -> {
            final var ai = entity.getData().attackAI;
            final var position = entity.getData().transform.position;

            if (ai.targetTagClass.equals(PlayerTag.class)) {
                maybeLocalPlayer.filter(player -> isTargetValid(player, position, ai))
                                .ifPresentOrElse(ai::setAttackTarget, ai::clearAttackTarget);
            } else {
                findOrUpdateTarget(resources.entities, ai, position);
            }

            updateAttackInput(entity, ai, position);
        });
    }

    private void updateAttackInput(
            final EntityDataHandle<EntityData> entity,
            final AttackAI ai,
            final Vector2d position
    ) {
        final var input = entity.getData().weaponInput;
        final var attackAbility = entity.getData().attackAbility;

        final var maybeTarget = ai.getAttackTarget();

        if (maybeTarget.isPresent()) {
            final var target = maybeTarget.get();
            final var targetPosition = target.getComponent(Transform.class).orElseThrow().position;

            if (wantsAttackTarget(ai, position, targetPosition)) {
                input.attack = true;
                // FIXME: Take aim lead with noise
                attackAbility.targetPosition = targetPosition;
                return;
            }
        }

        input.attack = false;
    }

    private boolean wantsAttackTarget(
            final AttackAI ai,
            final Vector2d position,
            final Vector2d targetPosition
    ) {
        final var attackRangeSq = ai.attackRange * ai.attackRange;
        final var distanceSq = targetPosition.distanceSquared(position);
        return distanceSq <= attackRangeSq;
    }

    private static void findOrUpdateTarget(
            final Entities entities,
            final AttackAI ai,
            final Vector2d position
    ) {
        final var maybeTarget = ai.getAttackTarget();
        if (maybeTarget.isEmpty() || !isTargetValid(maybeTarget.get(), position, ai)) {
            final var maybeNewTarget = findNewTarget(entities, ai, position);
            maybeNewTarget.ifPresentOrElse(ai::setAttackTarget, ai::clearAttackTarget);
        }
    }

    private static boolean isTargetValid(
            final EntityHandle target,
            final Vector2d position,
            final AttackAI ai
    ) {
        if (target.isPendingRemoval() || target.isDestroyed()) {
            return false;
        }
        if (!target.hasComponent(Transform.class)) {
            return false;
        }
        if (!target.hasComponent(ai.targetTagClass)) {
            return false;
        }

        final var targetPosition = target.getComponent(Transform.class).orElseThrow().position;
        return position.distanceSquared(targetPosition) <= ai.attackRange * ai.attackRange;
    }

    private static Optional<EntityHandle> findNewTarget(
            final Entities entities,
            final AttackAI ai,
            final Vector2d position
    ) {
        return entities.iterateEntities(new Class[]{ai.targetTagClass, Transform.class},
                                        new boolean[]{false, false},
                                        new boolean[]{false, false},
                                        objects -> null,
                                        false)
                       .map(EntityDataHandle::getHandle)
                       .filter(entity -> isTargetValid(entity, position, ai))
                       .findAny();
    }

    public static record Resources(Players players, Entities entities) {}

    public static record EntityData(
            AttackAI attackAI,
            WeaponInput weaponInput,
            AttackAbility attackAbility,
            Transform transform
    ) {}
}
