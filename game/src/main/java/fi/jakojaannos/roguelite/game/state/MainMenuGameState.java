package fi.jakojaannos.roguelite.game.state;

import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.systems.menu.HandleMainMenuUIEventsSystem;

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
        super(world);

        // FIXME: Pls. NO. Remove this once connect dialog is implemented
        this.uiEventsSystem.host = host;
        this.uiEventsSystem.port = port;
    }

    @Override
    protected SystemDispatcher createDispatcher() {
        final var builder = SystemDispatcher.builder();
        builder.group("main-menu")
               .withSystem(this.uiEventsSystem = new HandleMainMenuUIEventsSystem())
               .buildGroup();
        return builder.build();
    }

}
