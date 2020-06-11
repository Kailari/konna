package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;

import java.util.stream.Stream;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.LookAtTargetTag;

public class RotatePlayerTowardsAttackTargetSystem implements EcsSystem<EcsSystem.NoResources, RotatePlayerTowardsAttackTargetSystem.EntityData, EcsSystem.NoEvents> {
    private static final Vector2d ROTATION_ZERO_DIRECTION = new Vector2d(0.0, 1.0);

    @Override
    public void tick(
            final NoResources noResources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        entities.forEach(entity -> {
            final var transform = entity.getData().transform;
            final var attackAbility = entity.getData().attackAbility;

            final var position = transform.position.add(attackAbility.weaponOffset, new Vector2d());
            final var targetPosition = attackAbility.targetPosition;

            final Vector2d direction;
            if (position.lengthSquared() == 0 && targetPosition.lengthSquared() == 0) {
                direction = ROTATION_ZERO_DIRECTION;
            } else {
                direction = targetPosition.sub(position, new Vector2d())
                                          .normalize();
            }

            transform.rotation = -direction.angle(ROTATION_ZERO_DIRECTION);
        });
    }

    public static record EntityData(
            Transform transform,
            AttackAbility attackAbility,
            LookAtTargetTag lookAtTag
    ) {}
}
