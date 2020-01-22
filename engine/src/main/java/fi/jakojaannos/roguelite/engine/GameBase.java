package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.utilities.UpdateableTimeManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Queue;

@Slf4j
@RequiredArgsConstructor
public abstract class GameBase implements Game, MainThread {
    private boolean disposed = false;
    private boolean finished = false;

    private final Object taskQueueLock = new Object();
    private final Queue<MainThreadTask> mainThreadTaskQueue = new ArrayDeque<>();

    private final UpdateableTimeManager timeManager;

    public GameBase() {
        this(new SimpleTimeManager(20L));
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
