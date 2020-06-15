package fi.jakojaannos.riista;

import java.util.Collection;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameState;

public interface GameRenderAdapter<TPresentState> extends AutoCloseable {
    void onGameModeChange(GameMode gameMode, final GameState gameState);

    @Override
    void close();

    void writePresentableState(
            GameState gameState,
            Collection<Object> events
    );

    TPresentState fetchPresentableState();

    void preTick(GameState gameState);
}
