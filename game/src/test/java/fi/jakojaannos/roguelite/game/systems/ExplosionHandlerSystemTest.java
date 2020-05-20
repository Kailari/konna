package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.junit.jupiter.api.Test;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.resources.Explosions;
import fi.jakojaannos.roguelite.game.data.resources.RecentExplosion;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.*;

public class ExplosionHandlerSystemTest {
    private Explosions explosions;

    void beforeEach(final World world) {
        explosions = new Explosions();
        explosions.addExplosion(new RecentExplosion(new Vector2d(0, 0), 10, 10, 100, DamageSource.Generic.UNDEFINED));
        world.registerResource(explosions);
    }

    @Test
    void entitiesNearExplosionAreDamaged() {
        Health health = new Health(100, 100);

        whenGame().withSystems(new ExplosionHandlerSystem())
                  .withState(this::beforeEach)
                  .withState(state -> state.createEntity(new Transform(0, 0),
                                                         health,
                                                         new Physics.Builder().build()))
                  .runsSingleTick()
                  .expect(state -> assertEquals(1, health.damageInstances.size()));
    }

    @Test
    void entitiesNearExplosionArePushedAway() {
        Physics physics = Physics.builder().build();

        whenGame().withSystems(new ExplosionHandlerSystem())
                  .withState(this::beforeEach)
                  .withState(state -> state.createEntity(new Transform(-1, -1),
                                                         new Health(100, 100),
                                                         physics))
                  .runsSingleTick()
                  .expect(state -> assertAll(
                          // explosions happens at (0,0), entity is at (-1,-1) -> expect to move to negative dir
                          () -> assertTrue(physics.acceleration.x < 0),
                          () -> assertTrue(physics.acceleration.y < 0)
                  ));
    }

    @Test
    void entityCanBeDamagedFromMultipleExplosions() {
        Health health = new Health(100, 100);

        whenGame().withSystems(new ExplosionHandlerSystem())
                  .withState(this::beforeEach)
                  .withState(state -> {
                      explosions.addExplosion(new RecentExplosion(new Vector2d(1, 1), 1, 999, 10, DamageSource.Generic.UNDEFINED));
                      explosions.addExplosion(new RecentExplosion(new Vector2d(2, 2), 1, 999, 10, DamageSource.Generic.UNDEFINED));
                      explosions.addExplosion(new RecentExplosion(new Vector2d(3, 3), 1, 999, 10, DamageSource.Generic.UNDEFINED));
                      explosions.addExplosion(new RecentExplosion(new Vector2d(4, 4), 1, 999, 10, DamageSource.Generic.UNDEFINED));

                  })
                  .withState(state -> state.createEntity(new Transform(1, 1),
                                                         health,
                                                         Physics.builder().build()))
                  .runsSingleTick()
                  .expect(state -> assertEquals(5, health.damageInstances.size()));
    }

    @Test
    void explosionsCanDamageMultipleEntities() {
        Health[] hps = new Health[10];

        whenGame().withSystems(new ExplosionHandlerSystem())
                  .withState(this::beforeEach)
                  .withState(state -> {
                      for (int i = 0; i < hps.length; i++) {
                          hps[i] = new Health(100, 100);
                          state.createEntity(new Transform(1, 1),
                                             hps[i],
                                             Physics.builder().build());
                      }
                  })
                  .runsSingleTick()
                  .expect(state -> {
                      for (final Health hp : hps) {
                          assertEquals(1, hp.damageInstances.size());
                      }
                  });
    }

    @Test
    void entitiesAwayFromExplosionAreNotAffected() {
        Physics physics = Physics.builder().build();
        Health health = new Health(100, 100);

        whenGame().withSystems(new ExplosionHandlerSystem())
                  .withState(this::beforeEach)
                  .withState(state -> state.createEntity(new Transform(420, 666),
                                                         health,
                                                         physics))
                  .runsSingleTick()
                  .expect(state -> assertAll(
                          () -> assertEquals(0, physics.acceleration.lengthSquared()),
                          () -> assertEquals(0, health.damageInstances.size()),
                          () -> assertEquals(100, health.currentHealth))
                  );
    }

    // TODO:
    void friendlyEntitiesAreNotAffected() {

    }

    void friendlyEntitiesAreNotAffectedWhenNearEnemies() {

    }

    void enemiesAreAffectedWhenNearFriendlyEntities() {

    }
}
