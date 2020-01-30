package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.character.WalkingMovementAbility;
import fi.jakojaannos.roguelite.game.systems.characters.movement.CharacterMovementSystem;
import fi.jakojaannos.roguelite.game.systems.physics.ApplyVelocitySystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CharacterMovementSystemTest {
    private static final double EPSILON = 0.01;
    private static final double POSITION_EPSILON = 0.2;

    private CharacterMovementSystem system;
    private ApplyVelocitySystem applyVelocity;
    private World world;
    private Entity entity;
    private Velocity velocity;
    private Transform transform;
    private CharacterInput characterInput;
    private WalkingMovementAbility movementStats;

    @BeforeEach
    void beforeEach() {
        this.system = new CharacterMovementSystem();
        this.applyVelocity = new ApplyVelocitySystem();
        EntityManager entityManager = EntityManager.createNew(256, 32);
        this.world = World.createNew(entityManager);

        Time time = mock(Time.class);
        when(time.getTimeStepInSeconds()).thenReturn(0.02);
        world.createOrReplaceResource(Time.class, time);

        entity = entityManager.createEntity();
        this.characterInput = new CharacterInput();
        this.movementStats = new WalkingMovementAbility();
        this.velocity = new Velocity();
        this.transform = new Transform(0.0, 0.0);
        entityManager.addComponentTo(entity, this.transform);
        entityManager.addComponentTo(entity, this.velocity);
        entityManager.addComponentTo(entity, this.characterInput);
        entityManager.addComponentTo(entity, this.movementStats);

        this.world.getEntityManager().applyModifications();
    }

    @ParameterizedTest
    @CsvSource({"1.0f,10.0f", "2.0f,20.0f", "0.1f,1.0f"})
    void characterAcceleratesCorrectly(float acceleration, float expectedSpeedAfter10s) {
        this.movementStats.maxSpeed = Float.MAX_VALUE;
        this.movementStats.acceleration = acceleration;
        this.characterInput.move.x = 1.0f;
        for (int i = 0; i < 500; ++i) {
            this.system.tick(Stream.of(entity), this.world);
            this.applyVelocity.tick(Stream.of(entity), this.world);
        }

        assertEquals(expectedSpeedAfter10s, this.velocity.length(), EPSILON);
    }

    @ParameterizedTest
    @CsvSource({"-1.0f,1.0f,1.0f,10.0f", "1.0f,-1.0f,2.0f,20.0f", "-1.0f,-1.0f,0.1f,1.0f"})
    void characterAcceleratesCorrectly_diagonalInput(
            float inputH,
            float inputV,
            float acceleration,
            float expectedSpeedAfter10s
    ) {
        this.movementStats.maxSpeed = Float.MAX_VALUE;
        this.movementStats.acceleration = acceleration;
        this.characterInput.move.x = inputH;
        this.characterInput.move.y = inputV;
        for (int i = 0; i < 500; ++i) {
            this.system.tick(Stream.of(entity), this.world);
            this.applyVelocity.tick(Stream.of(entity), this.world);
        }

        assertEquals(expectedSpeedAfter10s, this.velocity.length(), EPSILON);
    }

    @ParameterizedTest
    @CsvSource({"-1.0f,0.75f,1.0f,10.0f", "1.0f,0.25f,2.0f,20.0f", "0.1f,0.0f,0.1f,1.0f"})
    void characterAcceleratesCorrectly_nonAxisAlignedInput(
            float inputH,
            float inputV,
            float acceleration,
            float expectedSpeedAfter10s
    ) {
        this.movementStats.maxSpeed = Float.MAX_VALUE;
        this.movementStats.acceleration = acceleration;
        this.characterInput.move.x = inputH;
        this.characterInput.move.y = inputV;
        for (int i = 0; i < 500; ++i) {
            this.system.tick(Stream.of(entity), this.world);
            this.applyVelocity.tick(Stream.of(entity), this.world);
        }

        assertEquals(expectedSpeedAfter10s, this.velocity.length(), EPSILON);
    }

    @ParameterizedTest
    @CsvSource({"1.0f,50.0f", "2.0f,100.0f", "0.1f,5.0f"})
    void characterPositionChangesCorrectly(float acceleration, float expectedPositionAfter10s) {
        this.movementStats.maxSpeed = Float.MAX_VALUE;
        this.movementStats.acceleration = acceleration;
        this.characterInput.move.x = 1.0f;
        for (int i = 0; i < 500; ++i) {
            this.system.tick(Stream.of(entity), this.world);
            this.applyVelocity.tick(Stream.of(entity), this.world);
        }

        assertEquals(expectedPositionAfter10s, this.transform.position.x, POSITION_EPSILON);
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
        this.movementStats.maxSpeed = Float.MAX_VALUE;
        this.movementStats.acceleration = acceleration;
        this.characterInput.move.x = inputH;
        this.characterInput.move.y = inputV;
        for (int i = 0; i < 500; ++i) {
            this.system.tick(Stream.of(entity), this.world);
            this.applyVelocity.tick(Stream.of(entity), this.world);
        }

        assertEquals(expectedX, this.transform.position.x, POSITION_EPSILON);
        assertEquals(expectedY, this.transform.position.y, POSITION_EPSILON);
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
        this.movementStats.maxSpeed = Float.MAX_VALUE;
        this.movementStats.acceleration = acceleration;
        this.characterInput.move.x = inputH;
        this.characterInput.move.y = inputV;
        for (int i = 0; i < 500; ++i) {
            this.system.tick(Stream.of(entity), this.world);
            this.applyVelocity.tick(Stream.of(entity), this.world);
        }

        assertEquals(expectedX, this.transform.position.x, POSITION_EPSILON);
        assertEquals(expectedY, this.transform.position.y, POSITION_EPSILON);
    }
}
