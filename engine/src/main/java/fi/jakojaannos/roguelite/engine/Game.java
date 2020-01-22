package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.state.WritableTimeProvider;

public interface Game extends WritableTimeProvider, AutoCloseable, MainThread {
    boolean isFinished();

    void setFinished(boolean state);

    boolean isDisposed();

    GameState tick(GameState state, Events events);
}
