package fi.jakojaannos.roguelite.game.systems.characters.movement;

import org.joml.Vector2d;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.legacy.*;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.character.JumpingMovementAbility;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

public class JumpingCharacterMovementSystem implements ECSSystem {
    private final Vector2d tmpForce = new Vector2d();

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CHARACTER_TICK)
                    .requireProvidedResource(Time.class)
                    .withComponent(JumpingMovementAbility.class)
                    .withComponent(MovementInput.class)
                    .withComponent(Physics.class)
                    .withoutComponent(InAir.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var entityManager = world.getEntityManager();
        final var timeManager = world.getResource(Time.class);

        entities.forEach(entity -> {
            final var movementAbility = entityManager.getComponentOf(entity, JumpingMovementAbility.class)
                                                     .orElseThrow();
            final var input = entityManager.getComponentOf(entity, MovementInput.class)
                                           .orElseThrow();

            if (isReadyToJump(timeManager, movementAbility)) {
                hopTowardsInputDirection(entity, entityManager, movementAbility, timeManager, input.move);
            }
        });
    }

    private boolean isReadyToJump(
            final Time timeManager,
            final JumpingMovementAbility movementAbility
    ) {
        final var timeSinceLastJump = timeManager.getCurrentGameTime() - movementAbility.lastJumpTimeStamp;
        return timeSinceLastJump >= movementAbility.jumpCoolDownInTicks;
    }

    private void hopTowardsInputDirection(
            final Entity entity,
            final EntityManager entityManager,
            final JumpingMovementAbility movementAbility,
            final TimeManager timeManager,
            final Vector2d input
    ) {
        final var physics = entityManager.getComponentOf(entity, Physics.class)
                                         .orElseThrow();

        tmpForce.set(input)
                .normalize(movementAbility.jumpForce);

        physics.applyForce(tmpForce);
        final var currentGameTime = timeManager.getCurrentGameTime();
        movementAbility.lastJumpTimeStamp = currentGameTime;
        entityManager.addComponentTo(entity,
                                     new InAir(currentGameTime,
                                               movementAbility.jumpDurationInTicks));
    }
}
