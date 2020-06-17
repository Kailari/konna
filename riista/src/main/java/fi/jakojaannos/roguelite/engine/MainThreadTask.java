package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.riista.GameState;

public interface MainThreadTask {
    void execute(GameState state);
}
