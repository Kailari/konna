package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;

@Slf4j
public abstract class GameBase<TState> implements Game<TState> {
    private final SimpleTimeManager timeManager = new SimpleTimeManager();

    private boolean disposed = false;
    private boolean finished = false;

    @NonNull
    @Override
    public TimeManager getTime() {
        return this.timeManager;
    }

    @Override
    public boolean isFinished() {
        return this.finished;
    }

    @Override
    public void setFinished(boolean state) {
        this.finished = state;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void tick(@NonNull TState state, @NonNull Queue<InputEvent> inputEvents, double delta) {
        this.timeManager.refresh();
    }

    @Override
    public void close() {
        if (this.disposed) {
            LOG.error(".close() called more than once for a game!");
            return;
        }
        this.disposed = true;
    }
}
