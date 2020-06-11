package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.junit.jupiter.api.Test;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.archetypes.PlayerArchetype;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.character.JumpingMovementAbility;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.systems.characters.movement.JumpingCharacterMovementSystem;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.junitextension.Assertions.assertEqualsExt;
import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.expectEntity;
import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;

public class JumpingCharacterMovementSystemTest {
    private static final double EPSILON = 0.001;
    private EntityHandle slime;
    private Vector2d expectedDirection;

    void initialState(final World world) {
        final var time = world.fetchResource(TimeManager.class);

        final var playerTransform = new Transform(10, 10);
        world.registerResource(new Players(PlayerArchetype.create(world, time, playerTransform)));

        final var slimeTransform = new Transform(3, 6);
        final var input = new MovementInput();
        this.expectedDirection = new Vector2d(playerTransform.position).sub(slimeTransform.position)
                                                                 .normalize();
        input.move = expectedDirection;
        this.slime = world.createEntity(slimeTransform,
                                        input,
                                        Physics.builder()
                                               .mass(2.5)
                                               .build(),
                                        JumpingMovementAbility.builder()
                                                              .jumpForce(5.0)
                                                              .build());
    }

    @Test
    void slimeHopsTowardsPlayer() {
        whenGame().withSystems(new JumpingCharacterMovementSystem())
                  .withState(this::initialState)
                  .runsSingleTick()
                  .expect(state -> {
                      expectEntity(slime).toHaveComponent(Physics.class)
                                         .which(physics -> assertEqualsExt(expectedDirection,
                                                                           physics.acceleration.normalize(),
                                                                           EPSILON));
                      expectEntity(slime).toHaveComponent(InAir.class);
                  });
    }
}
