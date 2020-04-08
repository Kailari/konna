package fi.jakojaannos.roguelite.game.test.global;

import org.joml.Vector2d;

import java.util.Optional;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.game.data.resources.Players;

public class GlobalGameState {
    public static Vector2d playerInitialPosition;
    public static Vector2d playerPositionBeforeRun;

    public static Optional<Entity> getLocalPlayer() {
        return Optional.ofNullable(GlobalState.state.world()
                                                    .fetchResource(Players.class).getPlayer());
    }

    public static void updatePlayerPositionBeforeRun() {
        playerPositionBeforeRun = getLocalPlayer().flatMap(player -> GlobalState.getComponentOf(player, Transform.class))
                                                  .map(transform -> new Vector2d(transform.position))
                                                  .orElse(new Vector2d(0.0));
    }
}
