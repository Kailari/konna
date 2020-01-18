package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HandleEntitiesInAirSystemTest {

    @Test
    void inAirComponentIsRemovedAfterExpiring() {
        EntityManager entityManager = EntityManager.createNew(256, 32);
        World world = World.createNew(entityManager);
        SimpleTimeManager timeManager = new SimpleTimeManager(20);
        world.createOrReplaceResource(Time.class, new Time(timeManager));
        HandleEntitiesInAirSystem system = new HandleEntitiesInAirSystem();

        Entity entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, new InAir(timeManager.getCurrentGameTime(), 10));
        entityManager.applyModifications();

        for (int i = 0; i < 10; i++) {
            timeManager.refresh();
        }
        system.tick(Stream.of(entity), world);

        assertFalse(entityManager.hasComponent(entity, InAir.class));
    }

    @Test
    void inAirComponentIsNotRemovedBeforeExpiring() {
        EntityManager entityManager = EntityManager.createNew(256, 32);
        World world = World.createNew(entityManager);
        SimpleTimeManager timeManager = new SimpleTimeManager(20);
        world.createOrReplaceResource(Time.class, new Time(timeManager));
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
        EntityManager entityManager = EntityManager.createNew(256, 32);
        World world = World.createNew(entityManager);
        SimpleTimeManager timeManager = new SimpleTimeManager(20);
        world.createOrReplaceResource(Time.class, new Time(timeManager));
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
