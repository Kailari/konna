package fi.jakojaannos.roguelite.engine.data.resources;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.engine.state.GameState;

@Slf4j
public class GameStateManager implements Resource {
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
            } catch (Exception e) {
                LOG.error("Error destroying the previous game state:", e);
            }
        }

        return current;
    }

    public void quitGame() {
        this.gameShouldClose = true;
    }
}
