package fi.jakojaannos.roguelite.game.systems.menu;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.data.resources.GameStateManager;
import fi.jakojaannos.roguelite.engine.data.resources.Network;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.network.client.ClientNetworkManager;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.game.data.resources.MainThread;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;

@Slf4j
public class HandleMainMenuUIEventsSystem implements ECSSystem {
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
        final var events = world.getResource(Events.class).getUi();
        while (events.hasEvents()) {
            final var event = events.pollEvent();
            if (event.getType() == UIEvent.Type.CLICK) {
                if (event.getElement().equalsIgnoreCase("play_button")) {
                    gameStateManager.queueStateChange(createGameplayState(world));
                } else if (event.getElement().equalsIgnoreCase("quit_button")) {
                    gameStateManager.quitGame();
                } else if (event.getElement().equalsIgnoreCase("connect_button")) {
                    if (this.host == null) {
                        return;
                    }

                    try {
                        final var state = createGameplayState(world);
                        state.setNetworkManager(new ClientNetworkManager(this.host,
                                                                         this.port,
                                                                         world.getOrCreateResource(MainThread.class)));
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
                                     World.createNew(EntityManager.createNew(256, 32)),
                                     world.getResource(Time.class).getTimeManager());
    }
}
