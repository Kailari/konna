package fi.jakojaannos.konna.view.adapters.menu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Stream;

import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.riista.view.ui.UiElement;
import fi.jakojaannos.roguelite.engine.MainThread;
import fi.jakojaannos.roguelite.engine.data.resources.Network;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.network.NetworkManager;
import fi.jakojaannos.roguelite.engine.network.client.ClientNetworkManager;
import fi.jakojaannos.roguelite.engine.state.StateEvent;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.gamemode.GameplayGameMode;

public class MainMenuRenderAdapter implements EcsSystem<MainMenuRenderAdapter.Resources, EcsSystem.NoEntities, EcsSystem.NoEvents> {
    private static final Logger LOG = LoggerFactory.getLogger(MainMenuRenderAdapter.class);

    private final UiElement mainMenu;

    public MainMenuRenderAdapter(final AssetManager assetManager) {
        this.mainMenu = assetManager.getStorage(UiElement.class)
                                    .getOrDefault("ui/main-menu.json");
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<NoEntities>> noEntities,
            final NoEvents noEvents
    ) {
        final var renderer = resources.renderer;
        final var network = resources.network;

        renderer.ui().setValue("NETWORK_CONNECTION_ERROR",
                               network.getConnectionError()
                                      .orElse(""));
        renderer.ui().setValue("NETWORK_CONNECTION_STATUS",
                               network.getNetworkManager()
                                      .filter(NetworkManager::isConnected)
                                      .map(ignored -> "Connected")
                                      .orElse("No connection"));

        final var uiEvents = renderer.ui().draw(this.mainMenu);

        final var stateEventBus = resources.events.state();
        uiEvents.forEach(event -> {
            if (event.type() == UIEvent.Type.CLICK) {
                if (event.element().equalsIgnoreCase("play-button")) {
                    stateEventBus.fire(new StateEvent.ChangeMode(GameplayGameMode.create(System.nanoTime(),
                                                                                         resources.timeManager)));
                } else if (event.element().equalsIgnoreCase("quit-button")) {
                    stateEventBus.fire(new StateEvent.Shutdown());
                } else if (event.element().equalsIgnoreCase("connect-button")) {
                    network.setConnectionError("");
                    try {
                        // FIXME: Fetch host/port from somewhere
                        //  -> should the `ui().draw(...)` construct the UI hierarchy/"state" so that
                        //     it could be queried here?
                        //  -> ^ Might be a bit overkill?
                        //  -> For now, just fetch using something like `ui().getValue(KEY)` and add
                        //     separate UI state (stored in a resource) or sth.
                        //  -> One option for first iteration is to make text fields labels with text
                        //     set via `ui().setValue(KEY)`. Then, when input occurs and the field
                        //     is active (which might be hard if UI is non-stateful? Click events
                        //     + something is required), relevant UI event is generated and characters
                        //     appended to the relevant string
                        final var networkManager = new ClientNetworkManager("127.0.0.1",
                                                                            18181,
                                                                            resources.mainThread);
                        network.setNetworkManager(networkManager);
                    } catch (final IOException e) {
                        LOG.error("Error connecting to server:", e);
                        network.setConnectionError(e.getMessage());
                    }
                }
            }
        });
    }

    public static record Resources(
            Renderer renderer,
            TimeManager timeManager,
            Network network,
            Events events,
            MainThread mainThread
    ) {}
}
