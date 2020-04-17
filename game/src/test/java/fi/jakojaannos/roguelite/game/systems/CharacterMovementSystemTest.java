package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.components.character.WalkingMovementAbility;
import fi.jakojaannos.roguelite.game.data.resources.collision.Colliders;
import fi.jakojaannos.roguelite.game.data.resources.collision.Collisions;
import fi.jakojaannos.roguelite.game.systems.characters.movement.CharacterMovementSystem;
import fi.jakojaannos.roguelite.game.systems.physics.ApplyVelocitySystem;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.junitextension.Assertions.assertEqualsExt;
import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacterMovementSystemTest {
    private static final double EPSILON = 0.01;
    private static final double POSITION_EPSILON = 0.25;

    private Velocity velocity;
    private Transform transform;
    private MovementInput movementInput;
    private WalkingMovementAbility movementStats;

    void beforeEach(final World world) {
        world.registerResource(new Colliders());
        world.registerResource(new Collisions());

        this.movementInput = new MovementInput();
        this.movementStats = new WalkingMovementAbility();
        this.velocity = new Velocity();
        this.transform = new Transform(0.0, 0.0);
        world.createEntity(this.transform,
                           this.velocity,
                           this.movementInput,
                           this.movementStats);
    }

    @ParameterizedTest
    @CsvSource({"1.0f,10.0f", "2.0f,20.0f", "0.1f,1.0f"})
    void characterAcceleratesCorrectly(float acceleration, float expectedSpeedAfter10s) {
        whenGame().withSystems(new CharacterMovementSystem(),
                               new ApplyVelocitySystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      this.movementStats.maxSpeed = Float.MAX_VALUE;
                      this.movementStats.acceleration = acceleration;
                      this.movementInput.move.x = 1.0f;
                  })
                  .runsForSeconds(10)
                  .expect(state -> assertEquals(expectedSpeedAfter10s, this.velocity.length(), EPSILON));
    }

    @ParameterizedTest
    @CsvSource({"-1.0f,1.0f,1.0f,10.0f", "1.0f,-1.0f,2.0f,20.0f", "-1.0f,-1.0f,0.1f,1.0f"})
    void characterAcceleratesCorrectly_diagonalInput(
            float inputH,
            float inputV,
            float acceleration,
            float expectedSpeedAfter10s
    ) {
        whenGame().withSystems(new CharacterMovementSystem(),
                               new ApplyVelocitySystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      this.movementStats.maxSpeed = Float.MAX_VALUE;
                      this.movementStats.acceleration = acceleration;
                      this.movementInput.move.x = inputH;
                      this.movementInput.move.y = inputV;
                  })
                  .runsForSeconds(10)
                  .expect(state -> assertEquals(expectedSpeedAfter10s, this.velocity.length(), EPSILON));
    }

    @ParameterizedTest
    @CsvSource({"-1.0f,0.75f,1.0f,10.0f", "1.0f,0.25f,2.0f,20.0f", "0.1f,0.0f,0.1f,1.0f"})
    void characterAcceleratesCorrectly_nonAxisAlignedInput(
            float inputH,
            float inputV,
            float acceleration,
            float expectedSpeedAfter10s
    ) {
        whenGame().withSystems(new CharacterMovementSystem(),
                               new ApplyVelocitySystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      this.movementStats.maxSpeed = Float.MAX_VALUE;
                      this.movementStats.acceleration = acceleration;
                      this.movementInput.move.x = inputH;
                      this.movementInput.move.y = inputV;
                  })
                  .runsForSeconds(10)
                  .expect(state -> assertEquals(expectedSpeedAfter10s, this.velocity.length(), EPSILON));
    }

    @ParameterizedTest
    @CsvSource({"1.0f,50.0f", "2.0f,100.0f", "0.1f,5.0f"})
    void characterPositionChangesCorrectly(float acceleration, float expectedPositionAfter10s) {
        whenGame().withSystems(new CharacterMovementSystem(),
                               new ApplyVelocitySystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      this.movementStats.maxSpeed = Float.MAX_VALUE;
                      this.movementStats.acceleration = acceleration;
                      this.movementInput.move.x = 1.0f;
                  })
                  .runsForSeconds(10)
                  .expect(state -> assertEquals(expectedPositionAfter10s, this.transform.position.x, POSITION_EPSILON));
    }

    @ParameterizedTest
    @CsvSource({"-1.0f,1.0f,1.0f,-35.355339f,35.355339f", "1.0f,-1.0f,2.0f,70.710678f,-70.710678f", "-1.0f,-1.0f,0.1f,-3.5355339f,-3.5355339f"})
    void characterPositionChangesCorrectly_diagonalInput(
            float inputH,
            float inputV,
            float acceleration,
            float expectedX,
            float expectedY
    ) {
        whenGame().withSystems(new CharacterMovementSystem(),
                               new ApplyVelocitySystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      this.movementStats.maxSpeed = Float.MAX_VALUE;
                      this.movementStats.acceleration = acceleration;
                      this.movementInput.move.x = inputH;
                      this.movementInput.move.y = inputV;
                  })
                  .runsForSeconds(10)
                  .expect(state -> assertEqualsExt(new Vector2d(expectedX, expectedY),
                                                   this.transform.position,
                                                   POSITION_EPSILON));
    }

    @ParameterizedTest
    @CsvSource({"-1.0,0.75,1.0,-40.0,30.0", "1.0,0.25,2.0,97.014250,24.253563"})
    void characterPositionChangesCorrectly_nonAxisAlignedInput(
            double inputH,
            double inputV,
            double acceleration,
            double expectedX,
            double expectedY
    ) {
        whenGame().withSystems(new CharacterMovementSystem(),
                               new ApplyVelocitySystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      this.movementStats.maxSpeed = Float.MAX_VALUE;
                      this.movementStats.acceleration = acceleration;
                      this.movementInput.move.x = inputH;
                      this.movementInput.move.y = inputV;
                  })
                  .runsForSeconds(10)
                  .expect(state -> assertEqualsExt(new Vector2d(expectedX, expectedY),
                                                   this.transform.position,
                                                   POSITION_EPSILON));
    }
}
