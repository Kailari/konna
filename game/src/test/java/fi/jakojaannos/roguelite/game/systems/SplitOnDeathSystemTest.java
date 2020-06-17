package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.api.Test;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.ecs.EntityHandle;
import fi.jakojaannos.riista.GameState;
import fi.jakojaannos.roguelite.game.data.archetypes.SlimeArchetype;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SplitOnDeath;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SplitOnDeathSystemTest {
    private EntityHandle slime;

    @Test
    void largeSlimeSpawnsMultipleSlimesOnDeath() {
        whenGame().withSystems(new SplitOnDeathSystem())
                  .withState(world -> {
                      slime = SlimeArchetype.createLargeSlime(world, new Transform(0.0, 0.0));
                  })
                  .runsSingleTick()
                  // There was exactly one slime at the beginning and it was not dead. Assert it has not split
                  .expect(state -> assertEquals(1, countSlimesInWorld(state)))
                  // Kill the slime
                  .then(state -> slime.addComponent(new DeadTag()))
                  .runsSingleTick()
                  // There was exactly one slime at the beginning. Assert that it has split.
                  .expect(state -> assertTrue(countSlimesInWorld(state) > 1));
    }

    private long countSlimesInWorld(final GameState state) {
        return state.world()
                    .iterateEntities(new Class[]{SplitOnDeath.class},
                                     new boolean[]{false},
                                     new boolean[]{false},
                                     objects -> null,
                                     false)
                    .count();
    }
}
