package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.systems.cleanup.ReaperSystem;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReaperSystemTest {


    @Test
    void reaperSystemRemovesEntityItReceives() {
        World world = fi.jakojaannos.roguelite.engine.ecs.newimpl.World.createNew();
        EntityManager entityManager = world.getEntityManager();
        ReaperSystem system = new ReaperSystem();

        Entity entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, new DeadTag());


        system.tick(Stream.of(entity), world);

        assertTrue(entity.isMarkedForRemoval());
    }

    @Test
    void reaperSystemRemovesAllEntitiesItReceives() {
        World world = fi.jakojaannos.roguelite.engine.ecs.newimpl.World.createNew();
        EntityManager entityManager = world.getEntityManager();
        ReaperSystem system = new ReaperSystem();

        Entity e1 = entityManager.createEntity();
        entityManager.addComponentTo(e1, new DeadTag());

        Entity e2 = entityManager.createEntity();
        entityManager.addComponentTo(e2, new DeadTag());

        Entity e3 = entityManager.createEntity();
        entityManager.addComponentTo(e3, new DeadTag());

        system.tick(Stream.of(e1, e2, e3), world);

        assertTrue(e1.isMarkedForRemoval());
        assertTrue(e2.isMarkedForRemoval());
        assertTrue(e3.isMarkedForRemoval());
    }


}
