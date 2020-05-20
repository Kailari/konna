package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.junit.jupiter.api.Test;

import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.resources.Explosions;
import fi.jakojaannos.roguelite.game.data.resources.RecentExplosion;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExplosionCleanupSystemTest {
    private Explosions explosions;

    void beforeEach(final World world) {
        explosions = new Explosions();
        world.registerResource(explosions);

        explosions.addExplosion(new RecentExplosion(new Vector2d(1, 2), 3.4, 5.6, 20, DamageSource.Generic.UNDEFINED));
    }

    @Test
    void explosionsAreCleanedUp() {
        whenGame().withSystems(new ExplosionCleanupSystem())
                  .withState(this::beforeEach)
                  .runsSingleTick()
                  .expect(state -> assertTrue(explosions.getExplosions().isEmpty()));
    }
}
