package fi.jakojaannos.roguelite.game.systems.characters.movement;

import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.character.WalkingMovementAbility;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;
import fi.jakojaannos.roguelite.game.systems.physics.ApplyFrictionSystem;

@Slf4j
public class CharacterMovementSystem implements ECSSystem {
    private static final float INPUT_EPSILON = 0.001f;

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CHARACTER_TICK)
                    .tickAfter(ApplyFrictionSystem.class)
                    .withComponent(Transform.class)
                    .withComponent(Velocity.class)
                    .withComponent(CharacterInput.class)
                    .withComponent(WalkingMovementAbility.class)
                    .withoutComponent(InAir.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var entityManager = world.getEntityManager();
        final var delta = world.getOrCreateResource(Time.class).getTimeStepInSeconds();

        entities.forEach(entity -> {
            final var input = entityManager.getComponentOf(entity, CharacterInput.class).orElseThrow();
            final var stats = entityManager.getComponentOf(entity, WalkingMovementAbility.class).orElseThrow();
            final var velocity = entityManager.getComponentOf(entity, Velocity.class).orElseThrow();

            if (input.move.lengthSquared() > INPUT_EPSILON * INPUT_EPSILON) {
                final var maxSpeed = (velocity.lengthSquared() >= stats.maxSpeed * stats.maxSpeed)
                        ? velocity.length()
                        : stats.maxSpeed;

                velocity.add(input.move.normalize(stats.acceleration * delta));
                if (velocity.lengthSquared() != 0) {
                    final var newSpeed = Math.min(velocity.length(), maxSpeed);
                    velocity.normalize(newSpeed);
                }
            }
        });
    }
}
