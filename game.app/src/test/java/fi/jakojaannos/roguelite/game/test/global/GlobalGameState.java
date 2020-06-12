package fi.jakojaannos.roguelite.game.test.global;

import org.joml.Vector2d;

import java.util.Optional;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.game.data.resources.Players;

public class GlobalGameState {
    public static Vector2d playerInitialPosition;
    public static Vector2d playerPositionBeforeRun;

    public static Optional<EntityHandle> getLocalPlayer() {
        return GlobalState.simulation.state()
                                     .world()
                                     .fetchResource(Players.class)
                                     .getLocalPlayer();
    }

    public static void updatePlayerPositionBeforeRun() {
        try {
            playerPositionBeforeRun = getLocalPlayer().flatMap(player -> player.getComponent(Transform.class))
                                                      .map(transform -> new Vector2d(transform.position))
                                                      .orElse(new Vector2d(0.0));
        } catch (Exception ignored) {
        }
    }
}
