package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.InAir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HandleEntitiesInAirSystemTest {
    private SimpleTimeManager time;
    private World world;
    private EntityManager entityManager;
    private HandleEntitiesInAirSystem system;

    @BeforeEach
    void beforeEach() {
        world = World.createNew();
        entityManager = world.getEntityManager();
        world.registerResource(TimeManager.class, time = new SimpleTimeManager(20));
        system = new HandleEntitiesInAirSystem();
    }

    @Test
    void inAirComponentIsRemovedAfterExpiring() {
        beforeEach();

        Entity entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, new InAir(time.getCurrentGameTime(), 10));
        entityManager.applyModifications();

        for (int i = 0; i < 20; i++) {
            time.refresh();
        }
        system.tick(Stream.of(entity), world);

        assertFalse(entityManager.hasComponent(entity, InAir.class));
    }

    @Test
    void inAirComponentIsNotRemovedBeforeExpiring() {
        Entity entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, new InAir(time.getCurrentGameTime(), 100));
        entityManager.applyModifications();

        for (int i = 0; i < 10; i++) {
            time.refresh();
        }
        system.tick(Stream.of(entity), world);

        assertTrue(entityManager.hasComponent(entity, InAir.class));
    }

    @Test
    void inAirComponentIsRemovedOnExactCorrectTick() {
        Entity entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, new InAir(time.getCurrentGameTime(), 5));
        entityManager.applyModifications();

        for (int i = 0; i < 5; i++) {
            time.refresh();
        }
        system.tick(Stream.of(entity), world);
        assertTrue(entityManager.hasComponent(entity, InAir.class));

        time.refresh();
        system.tick(Stream.of(entity), world);

        assertFalse(entityManager.hasComponent(entity, InAir.class));
    }

}
