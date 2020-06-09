package fi.jakojaannos.roguelite.engine.view;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameState;

public interface GameRenderer extends AutoCloseable {
    void render(GameState state, long accumulator);

    void changeGameMode(GameMode gameMode);
}
