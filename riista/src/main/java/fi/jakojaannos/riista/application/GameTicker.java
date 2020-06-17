package fi.jakojaannos.riista.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import fi.jakojaannos.riista.GameMode;
import fi.jakojaannos.riista.GameState;
import fi.jakojaannos.riista.data.events.StateEvent;
import fi.jakojaannos.riista.data.resources.Events;
import fi.jakojaannos.riista.data.resources.Network;
import fi.jakojaannos.riista.data.resources.StateEvents;
import fi.jakojaannos.riista.ecs.World;
import fi.jakojaannos.riista.input.InputProvider;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.MainThread;
import fi.jakojaannos.roguelite.engine.MainThreadTask;
import fi.jakojaannos.roguelite.engine.network.NetworkImpl;

public class GameTicker implements MainThread {
    private static final Logger LOG = LoggerFactory.getLogger(GameTicker.class);

    private final TimeManager timeManager;
    private final InputProvider inputProvider;
    private final List<StateEvent> stateEvents;
    private final List<Object> systemEvents;
    private final Network network;
    private final Object taskQueueLock = new Object();
    private final Queue<MainThreadTask> mainThreadTaskQueue = new ArrayDeque<>();

    private GameMode activeGameMode;
    private GameState currentState;

    public GameState getState() {
        return this.currentState;
    }

    public GameMode getMode() {
        return this.activeGameMode;
    }

    public Collection<Object> getSystemEvents() {
        return Collections.unmodifiableList(this.systemEvents);
    }

    public TimeManager getTimeManager() {
        return this.timeManager;
    }

    public GameTicker(
            final TimeManager timeManager,
            final InputProvider inputProvider,
            final GameMode initialGameMode
    ) {
        this.timeManager = timeManager;
        this.inputProvider = inputProvider;

        this.systemEvents = new ArrayList<>();
        this.stateEvents = new ArrayList<>();

        this.network = new NetworkImpl();

        changeActiveGameMode(initialGameMode);
    }

    public void changeActiveGameMode(final GameMode gameMode) {
        this.activeGameMode = gameMode;

        final var world = World.createNew();
        world.registerResource(Events.class, this.systemEvents::add);
        world.registerResource(StateEvents.class, this.stateEvents::add);
        world.registerResource(TimeManager.class, this.timeManager);
        world.registerResource(MainThread.class, this);
        world.registerResource(Network.class, this.network);

        this.activeGameMode.stateFactory().accept(world);
        this.currentState = new GameState(world, this.activeGameMode.systemStateFactory().get());
    }

    @Override
    public void queueTask(final MainThreadTask task) {
        synchronized (this.taskQueueLock) {
            this.mainThreadTaskQueue.offer(task);
        }
    }

    public void simulateTick(final Runnable terminateCallback) {
        synchronized (this.taskQueueLock) {
            while (!this.mainThreadTaskQueue.isEmpty()) {
                this.mainThreadTaskQueue.poll().execute(this.currentState);
            }
        }

        this.inputProvider.pollEvents()
                          .forEach(this.systemEvents::add);

        final var systemEventsFromLastTick = List.copyOf(this.systemEvents);
        this.systemEvents.clear();
        this.activeGameMode.systemDispatcher()
                           .tick(this.currentState.world(),
                                 this.currentState.systems(),
                                 systemEventsFromLastTick);

        for (final var stateEvent : this.stateEvents) {
            if (stateEvent instanceof StateEvent.ChangeMode changeMode) {
                if (this.activeGameMode != null) {
                    try {
                        this.activeGameMode.close();
                    } catch (final Exception e) {
                        LOG.error("Error while cleaning up old game mode: " + e.getMessage());
                    }
                }

                this.systemEvents.clear();
                changeActiveGameMode(changeMode.gameMode());
            } else if (stateEvent instanceof StateEvent.Shutdown) {
                terminateCallback.run();
            }
        }
        this.stateEvents.clear();
    }
}
