package fi.jakojaannos.roguelite.game.systems.menu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.MainThread;
import fi.jakojaannos.roguelite.engine.data.resources.GameStateManager;
import fi.jakojaannos.roguelite.engine.data.resources.Network;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.network.client.ClientNetworkManager;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;

public class HandleMainMenuUIEventsSystem implements ECSSystem {
    private static final Logger LOG = LoggerFactory.getLogger(HandleMainMenuUIEventsSystem.class);

    @Nullable public String host;
    public int port;

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.requireResource(GameStateManager.class)
                    .requireProvidedResource(Time.class)
                    .requireProvidedResource(Events.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var gameStateManager = world.getOrCreateResource(GameStateManager.class);
        final var events = world.getResource(Events.class).ui();
        while (events.hasEvents()) {
            final var event = events.pollEvent();
            if (event.type() == UIEvent.Type.CLICK) {
                if (event.element().equalsIgnoreCase("play_button")) {
                    gameStateManager.queueStateChange(createGameplayState(world));
                } else if (event.element().equalsIgnoreCase("quit_button")) {
                    gameStateManager.quitGame();
                } else if (event.element().equalsIgnoreCase("connect_button")) {
                    if (this.host == null) {
                        return;
                    }

                    try {
                        final var state = createGameplayState(world);
                        state.setNetworkManager(new ClientNetworkManager(this.host,
                                                                         this.port,
                                                                         world.getResource(MainThread.class)));
                        gameStateManager.queueStateChange(state);
                    } catch (final IOException e) {
                        LOG.error("Error connecting to server:", e);
                        world.getResource(Network.class)
                             .setConnectionError(e.getMessage());
                    }
                }
            }
        }
    }

    private GameplayGameState createGameplayState(final World world) {
        return new GameplayGameState(System.nanoTime(),
                                     fi.jakojaannos.roguelite.engine.ecs.newecs.World.createNew(),
                                     world.getResource(Time.class).timeManager());
    }
}
