package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fi.jakojaannos.riista.ecs.EntityHandle;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.InAir;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HandleEntitiesInAirSystemTest {
    private EntityHandle entity;

    @Test
    void inAirComponentIsRemovedAfterExpiring() {
        whenGame().withSystems(new HandleEntitiesInAirSystem())
                  .withState(world -> {
                      final var timeManager = world.fetchResource(TimeManager.class);
                      entity = world.createEntity(new InAir(timeManager.getCurrentGameTime(), 10));
                  })
                  .skipsTicks(20)
                  .runsSingleTick()
                  .expect(state -> assertFalse(entity.hasComponent(InAir.class)));
    }

    @Test
    void inAirComponentIsNotRemovedBeforeExpiring() {
        whenGame().withSystems(new HandleEntitiesInAirSystem())
                  .withState(world -> {
                      final var timeManager = world.fetchResource(TimeManager.class);
                      entity = world.createEntity(new InAir(timeManager.getCurrentGameTime(), 100));
                  })
                  .skipsTicks(20)
                  .runsSingleTick()
                  .expect(state -> assertTrue(entity.hasComponent(InAir.class)));
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 12345, 69})
    void inAirComponentIsRemovedOnExactCorrectTick(final int duration) {
        whenGame().withSystems(new HandleEntitiesInAirSystem())
                  .withState(world -> {
                      final var timeManager = world.fetchResource(TimeManager.class);
                      entity = world.createEntity(new InAir(timeManager.getCurrentGameTime(), duration));
                  })
                  .skipsTicks(duration)
                  .runsSingleTick()
                  .expect(state -> assertTrue(entity.hasComponent(InAir.class)))
                  .runsSingleTick()
                  .expect(state -> assertFalse(entity.hasComponent(InAir.class)));
    }
}
