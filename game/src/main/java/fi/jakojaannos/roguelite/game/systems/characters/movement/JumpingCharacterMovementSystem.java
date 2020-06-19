package fi.jakojaannos.roguelite.game.systems.characters.movement;

import org.joml.Vector2d;

import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.ecs.annotation.Without;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.character.JumpingMovementAbility;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;

public class JumpingCharacterMovementSystem implements EcsSystem<JumpingCharacterMovementSystem.Resources, JumpingCharacterMovementSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        entities.filter(this::wantsToMove)
                .filter(entity -> isReadyToJump(resources, entity))
                .forEach(entity -> hopTowardsInputDirection(resources, entity));
    }

    private boolean wantsToMove(final EntityDataHandle<EntityData> entity) {
        return entity.getData().input.move.lengthSquared() > 0;
    }

    private boolean isReadyToJump(final Resources resources, final EntityDataHandle<EntityData> entity) {
        final var ability = entity.getData().ability;

        final var timeSinceLastJump = resources.timeManager.getCurrentGameTime() - ability.lastJumpTimeStamp;
        return timeSinceLastJump >= ability.jumpCoolDownInTicks;
    }

    private void hopTowardsInputDirection(
            final Resources resources,
            final EntityDataHandle<EntityData> entity
    ) {
        final var ability = entity.getData().ability;
        final var input = entity.getData().input;

        entity.getData().physics.applyForce(input.move.normalize(ability.jumpForce, new Vector2d()));

        final var timestamp = resources.timeManager.getCurrentGameTime();
        ability.lastJumpTimeStamp = timestamp;
        entity.addComponent(new InAir(timestamp, ability.jumpDurationInTicks));
    }

    public static record Resources(TimeManager timeManager) {}

    public static record EntityData(
            JumpingMovementAbility ability,
            MovementInput input,
            Physics physics,
            @Without InAir noInAir
    ) {}
}
