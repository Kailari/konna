package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.junit.jupiter.api.Test;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.archetypes.PlayerArchetype;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.character.JumpingMovementAbility;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.systems.characters.movement.JumpingCharacterMovementSystem;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JumpingCharacterMovementSystemTest {
    private static final double EPSILON = 0.001;
    private EntityHandle slime;
    private Vector2d expectedDir;

    void initialState(final World world) {
        final var time = world.fetchResource(TimeManager.class);

        final var playerPos = new Transform(10, 10);
        final var players = new Players();
        players.setLocalPlayer(PlayerArchetype.create(world, time, playerPos).asLegacyEntity());
        world.registerResource(Players.class, players);

        final var slimeTransform = new Transform(3, 6);
        final var input = new MovementInput();
        this.expectedDir = new Vector2d(playerPos.position)
                .sub(slimeTransform.position)
                .normalize();
        input.move = expectedDir;
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
                  .withInitialState(this::initialState)
                  .runsForSingleTick()
                  .expect(state -> {
                      final var ability = slime.getComponent(JumpingMovementAbility.class).orElseThrow();
                      final var physics = slime.getComponent(Physics.class).orElseThrow();
                      final var expectedAcceleration = expectedDir.normalize(ability.jumpForce / physics.mass,
                                                                             new Vector2d());

                      assertEquals(expectedAcceleration.x, physics.acceleration.x, EPSILON);
                      assertEquals(expectedAcceleration.y, physics.acceleration.y, EPSILON);
                  });
    }
}
