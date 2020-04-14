package fi.jakojaannos.roguelite.game.systems.menu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.MainThread;
import fi.jakojaannos.roguelite.engine.data.resources.Network;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.network.client.ClientNetworkManager;
import fi.jakojaannos.roguelite.engine.state.StateEvent;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.gamemode.GameplayGameMode;

public class HandleMainMenuUIEventsSystem implements ECSSystem {
    private static final Logger LOG = LoggerFactory.getLogger(HandleMainMenuUIEventsSystem.class);

    @Nullable public String host;
    public int port;

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.requireProvidedResource(TimeManager.class)
                    .requireProvidedResource(Events.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var uiEvents = world.fetchResource(Events.class).ui();
        final var stateEvents = world.fetchResource(Events.class).state();
        while (uiEvents.hasEvents()) {
            final var event = uiEvents.pollEvent();
            if (event.type() == UIEvent.Type.CLICK) {
                if (event.element().equalsIgnoreCase("play_button")) {
                    stateEvents.fire(new StateEvent.ChangeMode(GameplayGameMode.create(System.nanoTime())));
                } else if (event.element().equalsIgnoreCase("quit_button")) {
                    stateEvents.fire(new StateEvent.Shutdown());
                } else if (event.element().equalsIgnoreCase("connect_button")) {
                    if (this.host == null) {
                        return;
                    }

                    try {
                        final var networkManager = new ClientNetworkManager(this.host,
                                                                            this.port,
                                                                            world.fetchResource(MainThread.class));
                        world.fetchResource(Network.class)
                             .setNetworkManager(networkManager);
                    } catch (final IOException e) {
                        LOG.error("Error connecting to server:", e);
                        world.fetchResource(Network.class)
                             .setConnectionError(e.getMessage());
                    }
                }
            }
        }
    }
}
