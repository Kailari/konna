package fi.jakojaannos.roguelite.game.state;

import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.RogueliteGame;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.systems.menu.HandleMainMenuUIEventsSystem;

public class MainMenuGameMode implements GameMode {
    private final SystemDispatcher systemDispatcher;

    public MainMenuGameMode() {
        this(null, -1);
    }

    public MainMenuGameMode(
            @Nullable final String host,
            final int port
    ) {
        final var builder = SystemDispatcher.builder();
        final var uiEventsSystem = new HandleMainMenuUIEventsSystem();
        builder.group("main-menu")
               .withSystem(uiEventsSystem)
               .buildGroup();
        this.systemDispatcher = builder.build();

        // FIXME: Pls. NO. Remove this once connect dialog is implemented
        uiEventsSystem.host = host;
        uiEventsSystem.port = port;
    }

    @Override
    public GameState createState(final World world) {
        final var state = new GameState(world);
        world.registerResource(new CameraProperties(null));
        world.registerResource(new Mouse());
        world.registerResource(new Inputs());

        return state;
    }

    @Override
    public void tick(final GameState state) {
        RogueliteGame.tickInputs(state);
        this.systemDispatcher.tick(state.world());
    }

    @Override
    public void close() throws Exception {
        this.systemDispatcher.close();
    }
}
