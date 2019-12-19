package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.state.TimeProvider;
import fi.jakojaannos.roguelite.engine.state.WorldProvider;
import fi.jakojaannos.roguelite.engine.state.WritableTimeProvider;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.utilities.UpdateableTimeManager;

import java.util.Queue;

public interface Game extends WritableTimeProvider, AutoCloseable {
    boolean isFinished();

    void setFinished(boolean state);

    boolean isDisposed();

    GameState tick(GameState state, Queue<InputEvent> inputEvents);
}
