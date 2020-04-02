package fi.jakojaannos.roguelite.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Queue;

import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.utilities.UpdateableTimeManager;

public abstract class GameBase implements Game, MainThread {
    private static final Logger LOG = LoggerFactory.getLogger(GameBase.class);

    private final Object taskQueueLock = new Object();
    private final Queue<MainThreadTask> mainThreadTaskQueue = new ArrayDeque<>();
    private final UpdateableTimeManager timeManager;
    private boolean disposed;
    private boolean finished;

    public GameBase() {
        this(new SimpleTimeManager(20L));
    }

    public GameBase(final UpdateableTimeManager timeManager) {
        this.timeManager = timeManager;
    }

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
        return this.disposed;
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
    public void queueTask(final MainThreadTask task) {
        synchronized (this.taskQueueLock) {
            this.mainThreadTaskQueue.offer(task);
        }
    }

    @Override
    public GameState tick(
            final GameState state,
            final Events events
    ) {
        synchronized (this.taskQueueLock) {
            while (!this.mainThreadTaskQueue.isEmpty()) {
                this.mainThreadTaskQueue.poll().execute(state);
            }
        }

        return state;
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
