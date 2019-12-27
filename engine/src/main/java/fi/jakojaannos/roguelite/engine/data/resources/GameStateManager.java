package fi.jakojaannos.roguelite.engine.data.resources;

import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.engine.state.GameState;
import lombok.val;

import javax.annotation.Nullable;

public class GameStateManager implements Resource {
    @Nullable private GameState newState;

    public void queueStateChange(final GameState newState) {
        this.newState = newState;
    }

    public GameState getNextState(final GameState current) {
        val nextState = this.newState != null
                ? this.newState
                : current;
        this.newState = null;
        return nextState;
    }
}
