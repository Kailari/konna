package fi.jakojaannos.roguelite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.MainThread;
import fi.jakojaannos.roguelite.engine.MainThreadTask;
import fi.jakojaannos.roguelite.engine.data.resources.Network;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.event.EventBus;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.event.RenderEvents;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.input.InputProvider;
import fi.jakojaannos.roguelite.engine.network.NetworkImpl;
import fi.jakojaannos.roguelite.engine.state.StateEvent;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

public class VulkanGameRunner implements MainThread {
    private static final Logger LOG = LoggerFactory.getLogger(VulkanGameRunner.class);

    private final TimeManager timeManager;
    private final InputProvider inputProvider;

    private final EventBus<InputEvent> inputBus;
    private final List<StateEvent> stateEvents;
    private final List<Object> systemEvents;
    private final Events events;

    private final Network network;

    @Deprecated
    protected final RenderEvents renderEvents;

    private final Object taskQueueLock = new Object();
    private final Queue<MainThreadTask> mainThreadTaskQueue = new ArrayDeque<>();

    private GameMode activeGameMode;
    private GameState currentState;

    public VulkanGameRunner(
            final TimeManager timeManager,
            final InputProvider inputProvider,
            final GameMode initialGameMode
    ) {
        this.timeManager = timeManager;
        this.inputProvider = inputProvider;

        this.inputBus = new EventBus<>();
        this.systemEvents = new ArrayList<>();
        this.stateEvents = new ArrayList<>();
        this.events = new Events(new EventBus<>(),
                                 this.inputBus,
                                 this.stateEvents::add,
                                 this.systemEvents::add);
        this.renderEvents = new RenderEvents(new ConcurrentLinkedQueue<>());

        this.network = new NetworkImpl();

        changeActiveGameMode(initialGameMode);
    }

    public void changeActiveGameMode(final GameMode gameMode) {
        this.activeGameMode = gameMode;

        final var world = World.createNew();
        world.registerResource(Events.class, this.events);
        world.registerResource(RenderEvents.class, this.renderEvents);
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
                          .forEach(this.inputBus::fire);

        final var systemEventsFromLastTick = List.copyOf(this.systemEvents);
        this.systemEvents.clear();
        this.activeGameMode.systemDispatcher()
                           .tick(this.currentState.world(),
                                 this.currentState.systems(),
                                 systemEventsFromLastTick);

        var stateHasChanged = false;
        var modeHasChanged = false;
        for (final var stateEvent : this.stateEvents) {
            if (stateEvent instanceof StateEvent.ChangeState changeState) {
                this.currentState = changeState.gameState();
                stateHasChanged = true;
            } else if (stateEvent instanceof StateEvent.ChangeMode changeMode) {
                if (this.activeGameMode != null) {
                    try {
                        this.activeGameMode.close();
                    } catch (final Exception e) {
                        LOG.error("Error while cleaning up old game mode: " + e.getMessage());
                    }
                }

                changeActiveGameMode(changeMode.gameMode());
                modeHasChanged = true;
            } else if (stateEvent instanceof StateEvent.Shutdown) {
                terminateCallback.run();
            }
        }

        if (modeHasChanged) {
            // TODO: is this required anymore?
            //onModeChange(this.activeGameMode);
        }

        if (stateHasChanged) {
            // TODO: is this required anymore?
            //onStateChange();
        }
    }
}
