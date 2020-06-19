package fi.jakojaannos.roguelite.game.gamemode;

import javax.annotation.Nullable;

import fi.jakojaannos.riista.data.resources.CameraProperties;
import fi.jakojaannos.riista.data.resources.Mouse;
import fi.jakojaannos.riista.GameMode;
import fi.jakojaannos.riista.ecs.SystemDispatcher;
import fi.jakojaannos.riista.ecs.World;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.systems.LegacyInputHandler;
import fi.jakojaannos.roguelite.game.systems.menu.HandleMainMenuUiEventsSystem;

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
        return new GameMode(GAME_MODE_ID,
                            createDispatcher(host, port),
                            MainMenuGameMode::createState);
    }

    private static SystemDispatcher createDispatcher(@Nullable final String host, final int port) {
        final var builder = SystemDispatcher.builder();
        final var uiEventsSystem = new HandleMainMenuUiEventsSystem();
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
        world.registerResource(new CameraProperties());
        world.registerResource(new Mouse());
        world.registerResource(new Inputs());
    }
}
