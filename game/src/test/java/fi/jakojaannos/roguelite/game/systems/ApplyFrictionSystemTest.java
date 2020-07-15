package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fi.jakojaannos.riista.ecs.EntityHandle;
import fi.jakojaannos.riista.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.resources.collision.Colliders;
import fi.jakojaannos.roguelite.game.data.resources.collision.Collisions;
import fi.jakojaannos.roguelite.game.systems.physics.ApplyFrictionSystem;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.*;

public class ApplyFrictionSystemTest {

    private static final double EPSILON = 0.01;

    private EntityHandle entity;
    private Velocity velocity;
    private Physics physics;

    void initialState(final World world) {
        world.registerResource(new Collisions());
        world.registerResource(new Colliders());

        entity = world.createEntity(physics = Physics.builder().friction(5.0).build(),
                                    velocity = new Velocity());
    }

    @ParameterizedTest
    @CsvSource({
                       "9.2, 13.6, 5.0, 6.4, 9.46",
                       "-15.4, -8.2, 3.2, -12.58, -6.7",
                       "0.2, -15.2, 10.8, 0.06, -4.4",
                       "2.0, 1.0, 8.0, 0.0, 0.0",
                       "0.0, 0.0, 1.0, 0.0, 0.0",
                       "0.0, 0.0, 0.0, 0.0, 0.0",
               })
    void frictionSlowsEntity(
            final double startX,
            final double startY,
            final double friction,
            final double expectedX,
            final double expectedY
    ) {
        whenGame().withSystems(new ApplyFrictionSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      physics.friction = friction;
                      velocity.set(startX, startY);
                  })
                  .runsForSeconds(1)
                  .expect(state -> {
                      assertEquals(expectedX, velocity.x, EPSILON);
                      assertEquals(expectedY, velocity.y, EPSILON);
                  });
    }

    @Test
    void zeroFrictionDoesNotSlowEntities() {
        whenGame().withSystems(new ApplyFrictionSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      physics.friction = 0;
                      velocity.set(12.34, 34.56);
                  })
                  .runsForTicks(10)
                  .expect(state -> {
                      assertEquals(12.34, velocity.x);
                      assertEquals(34.56, velocity.y);
                  });
    }

    @Test
    void frictionStopsEntityAfterAWhile() {
        whenGame().withSystems(new ApplyFrictionSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      velocity.set(12.34, 34.56);
                      physics.friction = 10.0f;
                  })
                  .runsForSeconds(5)
                  .expect(state -> {
                      assertEquals(0.0, velocity.x);
                      assertEquals(0.0, velocity.y);
                  });
    }

    @Test
    void dragSlowsEntity() {
        whenGame().withSystems(new ApplyFrictionSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      velocity.set(-69.69, 41.99);
                      physics.drag = 10.0;
                      physics.friction = 0.0;
                  })
                  .runsSingleTick()
                  .expect(state -> assertTrue(velocity.length() < new Vector2d(-69.69, 41.99).length()));
    }

    @Test
    void dragSlowsEntityInProportionToVelocity() {
        Velocity faster = new Velocity();

        whenGame().withSystems(new ApplyFrictionSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      velocity.set(10, 10);
                      physics.friction = 0.0;
                      physics.drag = 10.0;
                  })
                  .withState(world -> {
                      faster.set(100, 100);
                      final var phy2 = Physics.builder().friction(0.0).build();
                      phy2.drag = 10.0;
                      world.createEntity(faster,
                                         phy2);
                  })
                  .runsSingleTick()
                  .expect(state -> {
                      // assert: faster entity slowed down more
                      final double velocityDeltaSlow = new Vector2d(10, 10).length() - velocity.length();
                      final double velocityDeltaFast = new Vector2d(100, 100).length() - faster.length();

                      assertTrue(velocityDeltaFast > velocityDeltaSlow);
                  });
    }

    @Test
    void dragNearlyStopsEntityAfterWhile() {
        whenGame().withSystems(new ApplyFrictionSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      velocity.set(33.33, -20.00);
                      physics.drag = 10.0;
                      physics.friction = 0.0;
                  })
                  .runsForTicks(120)
                  .expect(state -> assertEquals(0.0, velocity.length(), EPSILON));
    }

    @Test
    void groundFrictionDoesntAffectFlyingEntity() {
        final double expectedLength = new Vector2d(11.11, 22.22).length();

        whenGame().withSystems(new ApplyFrictionSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      velocity.set(11.11, 22.22);
                      physics.drag = 0.0;
                      physics.friction = 100.0;
                      entity.addComponent(new InAir(0, 1000));
                  })
                  .runsForTicks(10)
                  .expect(state -> assertEquals(expectedLength, velocity.length(), EPSILON));
    }

    @Test
    void airFrictionSlowsFlyingEntity() {
        whenGame().withSystems(new ApplyFrictionSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      velocity.set(11.11, 22.22);
                      physics.drag = 10.0;
                      physics.friction = 0.0;
                      entity.addComponent(new InAir(0, 1000));
                  })
                  .runsForTicks(10)
                  .expect(state -> assertTrue(velocity.length() < new Vector2d(11.11, 22.22).length()));
    }
}
