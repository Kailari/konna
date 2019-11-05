package fi.jakojäännös.roguelite.engine;

import fi.jakojäännös.roguelite.engine.input.InputEvent;
import fi.jakojäännös.roguelite.engine.utilities.TimeManager;
import lombok.NonNull;

import java.util.Queue;

public interface Game extends AutoCloseable {
    @NonNull
    TimeManager getTime();

    boolean isFinished();

    void setFinished(boolean state);

    boolean isDisposed();

    void tick(Queue<InputEvent> inputEvents, double delta);
}
