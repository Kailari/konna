package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.roguelite.engine.state.GameState;

public interface MainThreadTask {
    void execute(GameState state);
}
