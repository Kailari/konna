package fi.jakojaannos.roguelite.game.state;

import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.systems.menu.HandleMainMenuUIEventsSystem;

import javax.annotation.Nullable;

public class MainMenuGameState extends GameState {
    private HandleMainMenuUIEventsSystem uiEventsSystem;

    public MainMenuGameState(
            final World world,
            final TimeManager timeManager
    ) {
        this(world, timeManager, null, -1);
    }

    public MainMenuGameState(
            final World world,
            final TimeManager timeManager,
            @Nullable final String host,
            final int port
    ) {
        super(world, timeManager);

        // FIXME: Pls. NO. Remove this once connect dialog is implemented
        this.uiEventsSystem.host = host;
        this.uiEventsSystem.port = port;
    }

    @Override
    protected SystemDispatcher createDispatcher() {
        return SystemDispatcher.builder()
                               .withSystem(this.uiEventsSystem = new HandleMainMenuUIEventsSystem())
                               .build();
    }

}
