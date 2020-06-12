package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.junit.jupiter.api.Test;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.weapon.Fuse;
import fi.jakojaannos.roguelite.game.data.components.weapon.GrenadeStats;
import fi.jakojaannos.roguelite.game.data.resources.Explosions;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.*;

public class GrenadeFuseUpdateSystemTest {
    private final double damage = 12;
    private final double radiusSquared = 69;
    private final Vector2d location = new Vector2d(123, 456);
    private Explosions explosions;
    private EntityHandle entity;

    void initialState(final World world) {
        explosions = new Explosions();
        world.registerResource(explosions);

        final var timeManager = world.fetchResource(TimeManager.class);

        entity = world.createEntity(
                new Transform(location),
                GrenadeStats.builder()
                            .fuseTime(20)
                            .explosionDamage(damage)
                            .explosionRadiusSquared(radiusSquared)
                            .build(),
                new Fuse(timeManager.getCurrentGameTime(), 20)
        );
    }

    @Test
    void explosionEntryIsAddedAfterFuseTimeIsUp() {
        whenGame().withSystems(new GrenadeFuseUpdateSystem())
                  .withState(this::initialState)
                  .runsForTicks(25)
                  .expect(state -> assertAll(
                          () -> assertEquals(1, explosions.getExplosions().size()),
                          () -> assertEquals(damage, explosions.getExplosions().get(0).damage()),
                          () -> assertEquals(radiusSquared, explosions.getExplosions().get(0).radiusSquared()),
                          () -> assertEquals(location, explosions.getExplosions().get(0).location())
                  ));
    }

    @Test
    void explodingEntityIsRemovedAfterFuseTimeIsUp() {
        whenGame().withSystems(new GrenadeFuseUpdateSystem())
                  .withState(this::initialState)
                  .runsForTicks(25)
                  .expect(state -> assertTrue(entity.isPendingRemoval()));
    }

    @Test
    void explosionEntryIsNotAddedPrematurely() {
        whenGame().withSystems(new GrenadeFuseUpdateSystem())
                  .withState(this::initialState)
                  .runsForTicks(15)
                  .expect(state -> assertTrue(explosions.getExplosions().isEmpty()));
    }

    @Test
    void explodingEntityIsNotRemovedPrematurely() {
        whenGame().withSystems(new GrenadeFuseUpdateSystem())
                  .withState(this::initialState)
                  .runsForTicks(15)
                  .expect(state -> assertFalse(entity.isPendingRemoval()));
    }
}
