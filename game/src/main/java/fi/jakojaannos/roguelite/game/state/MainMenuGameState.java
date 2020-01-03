package fi.jakojaannos.roguelite.game.state;

import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.systems.menu.HandleMainMenuUIEventsSystem;

public class MainMenuGameState extends GameState {
    public MainMenuGameState(
            final World world,
            final TimeManager timeManager
    ) {
        super(world, timeManager);
    }

    @Override
    protected SystemDispatcher createDispatcher() {
        return SystemDispatcher.builder()
                               .withSystem(new HandleMainMenuUIEventsSystem())
                               .build();
    }

}
