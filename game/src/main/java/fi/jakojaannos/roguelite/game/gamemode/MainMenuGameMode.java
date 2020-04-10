package fi.jakojaannos.roguelite.game.gamemode;

import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.systems.LegacyInputHandler;
import fi.jakojaannos.roguelite.game.systems.menu.HandleMainMenuUIEventsSystem;

public final class MainMenuGameMode {
    public static final int GAME_MODE_ID = 1;

    private MainMenuGameMode() {
    }

    public static GameMode create() {
        return create(null, -1);
    }

    public static GameMode create(
            @Nullable final String host,
            final int port
    ) {
        final var dispatcher = createDispatcher(host, port);
        return new GameMode(GAME_MODE_ID, dispatcher, MainMenuGameMode::createState);
    }

    private static SystemDispatcher createDispatcher(@Nullable final String host, final int port) {
        final var builder = SystemDispatcher.builder();
        final var uiEventsSystem = new HandleMainMenuUIEventsSystem();
        builder.group("main-menu")
               .withSystem(new LegacyInputHandler())
               .withSystem(uiEventsSystem)
               .buildGroup();
        final var dispatcher = builder.build();

        // FIXME: Pls. NO. Remove this once connect dialog is implemented
        uiEventsSystem.host = host;
        uiEventsSystem.port = port;
        return dispatcher;
    }

    private static void createState(final World world) {
        world.registerResource(new CameraProperties(null));
        world.registerResource(new Mouse());
        world.registerResource(new Inputs());
    }
}
