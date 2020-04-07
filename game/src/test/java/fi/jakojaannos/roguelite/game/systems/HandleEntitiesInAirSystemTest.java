package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.legacy.LegacyWorld;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.game.data.components.InAir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HandleEntitiesInAirSystemTest {

    @Test
    void inAirComponentIsRemovedAfterExpiring() {
        LegacyWorld world = World.createNew();
        EntityManager entityManager = world.getEntityManager();
        SimpleTimeManager timeManager = new SimpleTimeManager(20);
        world.provideResource(Time.class, new Time(timeManager));
        HandleEntitiesInAirSystem system = new HandleEntitiesInAirSystem();

        Entity entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, new InAir(timeManager.getCurrentGameTime(), 10));
        entityManager.applyModifications();

        for (int i = 0; i < 20; i++) {
            timeManager.refresh();
        }
        system.tick(Stream.of(entity), world);

        assertFalse(entityManager.hasComponent(entity, InAir.class));
    }

    @Test
    void inAirComponentIsNotRemovedBeforeExpiring() {
        LegacyWorld world = World.createNew();
        EntityManager entityManager = world.getEntityManager();
        SimpleTimeManager timeManager = new SimpleTimeManager(20);
        world.provideResource(Time.class, new Time(timeManager));
        HandleEntitiesInAirSystem system = new HandleEntitiesInAirSystem();

        Entity entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, new InAir(timeManager.getCurrentGameTime(), 100));
        entityManager.applyModifications();

        for (int i = 0; i < 10; i++) {
            timeManager.refresh();
        }
        system.tick(Stream.of(entity), world);

        assertTrue(entityManager.hasComponent(entity, InAir.class));
    }

    @Test
    void inAirComponentIsRemovedOnExactCorrectTick() {
        LegacyWorld world = World.createNew();
        EntityManager entityManager = world.getEntityManager();
        SimpleTimeManager timeManager = new SimpleTimeManager(20);
        world.provideResource(Time.class, new Time(timeManager));
        HandleEntitiesInAirSystem system = new HandleEntitiesInAirSystem();

        Entity entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, new InAir(timeManager.getCurrentGameTime(), 5));
        entityManager.applyModifications();

        for (int i = 0; i < 5; i++) {
            timeManager.refresh();
        }
        system.tick(Stream.of(entity), world);
        assertTrue(entityManager.hasComponent(entity, InAir.class));

        timeManager.refresh();
        system.tick(Stream.of(entity), world);

        assertFalse(entityManager.hasComponent(entity, InAir.class));
    }

}
