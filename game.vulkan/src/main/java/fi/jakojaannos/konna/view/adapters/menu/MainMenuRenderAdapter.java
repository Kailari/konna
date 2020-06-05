package fi.jakojaannos.konna.view.adapters.menu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Stream;

import fi.jakojaannos.konna.engine.assets.AssetManager;
import fi.jakojaannos.konna.engine.view.Renderer;
import fi.jakojaannos.konna.engine.view.ui.UiElement;
import fi.jakojaannos.roguelite.engine.MainThread;
import fi.jakojaannos.roguelite.engine.data.resources.Network;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.network.client.ClientNetworkManager;
import fi.jakojaannos.roguelite.engine.state.StateEvent;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
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
        final var uiEvents = resources.renderer.ui().draw(this.mainMenu);

        final var stateEventBus = resources.events.state();
        uiEvents.forEach(event -> {
            if (event.type() == UIEvent.Type.CLICK) {
                if (event.element().equalsIgnoreCase("play-button")) {
                    stateEventBus.fire(new StateEvent.ChangeMode(GameplayGameMode.create(System.nanoTime(),
                                                                                         resources.timeManager)));
                } else if (event.element().equalsIgnoreCase("quit-button")) {
                    stateEventBus.fire(new StateEvent.Shutdown());
                } else if (event.element().equalsIgnoreCase("connect-button")) {
                    try {
                        // FIXME: Fetch host/port from somewhere
                        final var networkManager = new ClientNetworkManager("127.0.0.1",
                                                                            18181,
                                                                            resources.mainThread);
                        resources.network.setNetworkManager(networkManager);
                    } catch (final IOException e) {
                        LOG.error("Error connecting to server:", e);
                        resources.network.setConnectionError(e.getMessage());
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
