package fi.jakojaannos.roguelite.game.systems;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.SpawnerComponent;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.FollowerAI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SpawnerSystemTest {
    private SpawnerSystem spawnerSystem;
    private World world;
    private EntityManager entityManager;


    @BeforeEach
    void beforeEach() {
        this.spawnerSystem = new SpawnerSystem();
        this.entityManager = EntityManager.createNew(256, 32);
        this.world = World.createNew(entityManager);

        final var time = new Time(new SimpleTimeManager(20));
        world.provideResource(Time.class, time);

        entityManager.applyModifications();
    }


    @Test
    void spawnerCreatesCorrectAmountOfEnemies(
    ) {
        double spawnFrequency = 1.0;
        int nTicks = 100;
        double delta = 0.02;
        long expectedAmount = 2;

        Entity spawner = this.entityManager.createEntity();
        entityManager.addComponentTo(spawner, new Transform());
        entityManager.addComponentTo(spawner,
                                     new SpawnerComponent(spawnFrequency, (entities, spawnerPos, spawnerComponent) -> {
                                         Entity e = entities.createEntity();
                                         entities.addComponentTo(e, new FollowerAI(0, 0));
                                         return e;
                                     }));

        entityManager.applyModifications();

        long enemiesBefore = this.world.getEntityManager().getEntitiesWith(FollowerAI.class).count();

        for (int i = 0; i < nTicks; i++) {
            this.spawnerSystem.tick(Stream.of(spawner), this.world);
        }

        entityManager.applyModifications();

        long enemiesAfter = this.world.getEntityManager().getEntitiesWith(FollowerAI.class).count();

        assertEquals(expectedAmount, (enemiesAfter - enemiesBefore));
    }

    @Test
    void entityFactoryIsCalledCorrectly() {
        Entity spawnedEnemy = mock(Entity.class);
        SpawnerComponent.EntityFactory mockFactory = mock(SpawnerComponent.EntityFactory.class);
        when(mockFactory.get(any(), any(), any())).thenReturn(spawnedEnemy);

        SpawnerComponent spawnerComponent = new SpawnerComponent(1.0f, mockFactory);
        Entity spawner = entityManager.createEntity();
        entityManager.addComponentTo(spawner, spawnerComponent);
        entityManager.addComponentTo(spawner, new Transform());

        for (int i = 0; i < 200; i++) {
            spawnerSystem.tick(Stream.of(spawner), world);
        }

        verify(mockFactory, times(4)).get(eq(entityManager), any(), eq(spawnerComponent));

    }

}
