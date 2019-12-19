package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.utilities.UpdateableTimeManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class GameBase implements Game {
    private boolean disposed = false;
    private boolean finished = false;

    private final UpdateableTimeManager timeManager = new SimpleTimeManager(20L);

    @Override
    public boolean isFinished() {
        return this.finished;
    }

    @Override
    public void setFinished(final boolean state) {
        this.finished = state;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void updateTime() {
        this.timeManager.refresh();
    }

    @Override
    public TimeManager getTime() {
        return this.timeManager;
    }

    @Override
    public void close() {
        if (this.disposed) {
            LOG.error(".close() called more than once for a game instance!");
            return;
        }
        this.disposed = true;
    }
}
