package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.DeadTag;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReaperSystemTest {


    @Test
    void reaperSystemRemovesEntityItReceives() {
        EntityManager entityManager = EntityManager.createNew(256, 32);
        World world = World.createNew(entityManager);
        ReaperSystem system = new ReaperSystem();

        Entity entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, new DeadTag());


        system.tick(Stream.of(entity), world, 0.2f);

        assertTrue(entity.isMarkedForRemoval());
    }

    @Test
    void reaperSystemRemovesAllEntitiesItReceives() {
        EntityManager entityManager = EntityManager.createNew(256, 32);
        World world = World.createNew(entityManager);
        ReaperSystem system = new ReaperSystem();

        Entity e1 = entityManager.createEntity();
        entityManager.addComponentTo(e1, new DeadTag());

        Entity e2 = entityManager.createEntity();
        entityManager.addComponentTo(e2, new DeadTag());

        Entity e3 = entityManager.createEntity();
        entityManager.addComponentTo(e3, new DeadTag());

        system.tick(Stream.of(e1, e2, e3), world, 0.2f);

        assertTrue(e1.isMarkedForRemoval());
        assertTrue(e2.isMarkedForRemoval());
        assertTrue(e3.isMarkedForRemoval());
    }


}
