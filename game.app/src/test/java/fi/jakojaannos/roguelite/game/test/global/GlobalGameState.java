package fi.jakojaannos.roguelite.game.test.global;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import org.joml.Vector2d;

import java.util.Optional;

public class GlobalGameState {
    public static Vector2d playerInitialPosition;
    public static Vector2d playerPositionBeforeRun;

    public static Optional<Entity> getLocalPlayer() {
        return Optional.ofNullable(GlobalState.state.getWorld()
                                                    .getResource(Players.class).player);
    }

    public static void updatePlayerPositionBeforeRun() {
        playerPositionBeforeRun = getLocalPlayer().flatMap(player -> GlobalState.getComponentOf(player, Transform.class))
                                                  .map(transform -> new Vector2d(transform.position))
                                                  .orElseThrow();
    }
}
