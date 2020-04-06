package fi.jakojaannos.roguelite.engine.data.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Resource;
import fi.jakojaannos.roguelite.engine.state.GameState;

public class GameStateManager implements Resource {
    private static final Logger LOG = LoggerFactory.getLogger(GameStateManager.class);

    @Nullable private GameState newState;
    private boolean gameShouldClose;

    public boolean shouldShutDown() {
        return this.gameShouldClose;
    }

    public void queueStateChange(final GameState newState) {
        this.newState = newState;
    }

    public GameState getNextState(final GameState current) {
        if (this.newState != null) {
            try {
                current.getNetworkManager()
                       .ifPresent(netman -> {
                           this.newState.setNetworkManager(netman);
                           current.setNetworkManager(null);
                       });
                current.close();

                final var newState = this.newState;
                this.newState = null;
                return newState;
            } catch (final Exception e) {
                LOG.error("Error destroying the previous game state:", e);
            }
        }

        return current;
    }

    public void quitGame() {
        this.gameShouldClose = true;
    }
}
