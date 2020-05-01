package fi.jakojaannos.roguelite.game.systems;


import org.junit.jupiter.api.Test;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.data.resources.Entities;
import fi.jakojaannos.roguelite.game.data.components.SpawnerComponent;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SpawnerSystemTest {
    private SpawnerComponent spawner;
    private SpawnerComponent.EntityFactory mockFactory;

    void beforeEach(final World world) {
        double spawnFrequency = 1.0;
        mockFactory = mock(SpawnerComponent.EntityFactory.class);
        spawner = new SpawnerComponent(spawnFrequency, mockFactory);
        world.createEntity(new Transform(), spawner);
    }

    @Test
    void spawnerTriggersCorrectNumberOfSpawns() {
        int nTicks = 100;
        int expectedSpawns = 2;

        whenGame().withSystems(new SpawnerSystem())
                  .withState(this::beforeEach)
                  .withSystemState(systemState -> systemState.setState(SpawnerSystem.class, true))
                  .runsForTicks(nTicks)
                  .expect(state -> verify(mockFactory, times(expectedSpawns)).get(any(), any(), any()));
    }

    @Test
    void entityFactoryIsCalledWithExpectedParameters() {
        whenGame().withSystems(new SpawnerSystem())
                  .withState(this::beforeEach)
                  .withSystemState(systemState -> systemState.setState(SpawnerSystem.class, true))
                  .runsForTicks(200)
                  .expect(state -> {
                      final var entities = state.world().fetchResource(Entities.class);
                      verify(mockFactory, times(4)).get(eq(entities), any(), eq(spawner));
                  });
    }
}
