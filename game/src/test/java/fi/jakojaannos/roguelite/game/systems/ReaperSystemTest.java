package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.api.Test;

import fi.jakojaannos.riista.ecs.EntityHandle;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.systems.cleanup.ReaperSystem;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReaperSystemTest {
    private EntityHandle entityA;
    private EntityHandle entityB;
    private EntityHandle entityC;

    @Test
    void reaperSystemRemovesEntityItReceives() {
        whenGame().withSystems(new ReaperSystem())
                  .withState(world -> entityA = world.createEntity(new DeadTag()))
                  .runsSingleTick()
                  .expect(state -> assertTrue(entityA.isPendingRemoval()));
    }

    @Test
    void reaperSystemRemovesAllEntitiesItReceives() {
        whenGame().withSystems(new ReaperSystem())
                  .withState(world -> {
                      entityA = world.createEntity(new DeadTag());
                      entityB = world.createEntity(new DeadTag());
                      entityC = world.createEntity(new DeadTag());
                  })
                  .runsSingleTick()
                  .expect(state -> assertAll(() -> assertTrue(entityA.isPendingRemoval()),
                                             () -> assertTrue(entityB.isPendingRemoval()),
                                             () -> assertTrue(entityC.isPendingRemoval())));
    }
}
