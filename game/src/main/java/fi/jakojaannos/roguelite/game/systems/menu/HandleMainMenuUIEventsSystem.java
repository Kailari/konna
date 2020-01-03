package fi.jakojaannos.roguelite.game.systems.menu;

import fi.jakojaannos.roguelite.engine.data.resources.GameStateManager;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;
import lombok.val;

import java.util.stream.Stream;

public class HandleMainMenuUIEventsSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.requireResource(GameStateManager.class)
                    .requireResource(Time.class)
                    .requireResource(Events.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val gameStateManager = world.getOrCreateResource(GameStateManager.class);
        val events = world.getOrCreateResource(Events.class).getUi();
        while (events.hasEvents()) {
            val event = events.pollEvent();
            if (event.getType() == UIEvent.Type.CLICK) {
                if (event.getElement().equalsIgnoreCase("play_button")) {
                    gameStateManager.queueStateChange(new GameplayGameState(System.nanoTime(),
                                                                            World.createNew(EntityManager.createNew(256, 32)),
                                                                            world.getOrCreateResource(Time.class).getTimeManager()));
                } else if (event.getElement().equalsIgnoreCase("quit_button")) {
                    gameStateManager.quitGame();
                }
            }
        }
    }
}
