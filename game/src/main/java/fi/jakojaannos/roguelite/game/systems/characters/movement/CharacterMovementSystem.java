package fi.jakojaannos.roguelite.game.systems.characters.movement;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.annotation.Without;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.components.character.WalkingMovementAbility;

public class CharacterMovementSystem implements EcsSystem<CharacterMovementSystem.Resources, CharacterMovementSystem.EntityData, EcsSystem.NoEvents> {
    private static final float INPUT_EPSILON = 0.001f;

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var delta = resources.timeManager.getTimeStepInSeconds();

        entities.forEach(entity -> {
            final var input = entity.getData().input;
            final var ability = entity.getData().ability;
            final var velocity = entity.getData().velocity;

            if (input.move.lengthSquared() > INPUT_EPSILON * INPUT_EPSILON) {
                final var movingFasterThanMaxSpeed = velocity.lengthSquared() >= ability.maxSpeed * ability.maxSpeed;
                final var actualMaxSpeed = movingFasterThanMaxSpeed
                        ? velocity.length()
                        : ability.maxSpeed;

                velocity.add(input.move.normalize(ability.acceleration * delta));
                if (velocity.lengthSquared() != 0) {
                    final var newSpeed = Math.min(velocity.length(), actualMaxSpeed);
                    velocity.normalize(newSpeed);
                }
            }
        });
    }

    public static record Resources(TimeManager timeManager) {}

    public static record EntityData(
            Transform transform,
            Velocity velocity,
            MovementInput input,
            WalkingMovementAbility ability,
            @Without InAir noInAir
    ) {}
}
