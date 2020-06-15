package fi.jakojaannos.roguelite.game.systems.menu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import fi.jakojaannos.riista.data.events.UiEvent;
import fi.jakojaannos.riista.data.resources.Network;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.MainThread;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.network.client.ClientNetworkManager;
import fi.jakojaannos.riista.data.events.StateEvent;
import fi.jakojaannos.roguelite.game.gamemode.GameplayGameMode;

public class HandleMainMenuUiEventsSystem implements EcsSystem<HandleMainMenuUiEventsSystem.Resources, EcsSystem.NoEntities, HandleMainMenuUiEventsSystem.EventData> {
    private static final Logger LOG = LoggerFactory.getLogger(HandleMainMenuUiEventsSystem.class);

    @Nullable public String host;
    public int port;

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<NoEntities>> entities,
            final EventData eventData
    ) {
        final var timeManager = resources.timeManager;
        final var stateEvents = resources.events.state();

        eventData.events.forEach(event -> {
            if (event.type() == UiEvent.Type.CLICK) {
                if (event.element().equalsIgnoreCase("play-button")) {
                    stateEvents.fire(new StateEvent.ChangeMode(GameplayGameMode.create(System.nanoTime(),
                                                                                       timeManager)));
                } else if (event.element().equalsIgnoreCase("quit-button")) {
                    stateEvents.fire(new StateEvent.Shutdown());
                } else if (event.element().equalsIgnoreCase("connect-button")) {
                    if (this.host == null) {
                        return;
                    }

                    try {
                        final var networkManager = new ClientNetworkManager(this.host,
                                                                            this.port,
                                                                            resources.mainThread);
                        resources.network.setNetworkManager(networkManager);
                    } catch (final IOException e) {
                        LOG.error("Error connecting to the server:", e);
                        resources.network.setConnectionError(e.getMessage());
                    }
                }
            }
        });
    }

    public static record Resources(
            TimeManager timeManager,
            MainThread mainThread,
            Network network,
            Events events
    ) {}

    public static record EventData(Iterable<UiEvent>events) {}
}
