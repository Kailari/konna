package fi.jakojaannos.konna.view.adapters.menu;

import java.util.stream.Stream;

import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.data.resources.Network;
import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.riista.view.ui.UiElement;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.data.resources.Events;
import fi.jakojaannos.roguelite.engine.network.NetworkManager;

public class MainMenuRenderAdapter implements EcsSystem<MainMenuRenderAdapter.Resources, EcsSystem.NoEntities, EcsSystem.NoEvents> {
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
        uiEvents.forEach(resources.events.system()::fire);
    }

    public static record Resources(
            Renderer renderer,
            Network network,
            Events events
    ) {}
}
