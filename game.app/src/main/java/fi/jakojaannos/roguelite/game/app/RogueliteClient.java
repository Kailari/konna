package fi.jakojaannos.roguelite.game.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLAssetManager;
import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLGameRunner;
import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLRenderingBackend;
import fi.jakojaannos.roguelite.engine.lwjgl.input.LWJGLInputProvider;
import fi.jakojaannos.roguelite.game.DebugConfig;
import fi.jakojaannos.roguelite.game.gamemode.MainMenuGameMode;
import fi.jakojaannos.roguelite.game.view.RogueliteGameRenderer;

public class RogueliteClient {
    private static final Logger LOG = LoggerFactory.getLogger(RogueliteClient.class);

    public static void run(
            final Path assetRoot,
            final String host,
            final int port,
            final int windowWidth,
            final int windowHeight
    ) throws Exception {
        LOG.trace("Running application");
        LOG.debug("asset root: {}", assetRoot);

         try (final var runner = new LWJGLGameRunner(DebugConfig.debugModeEnabled,
                                                    DebugConfig.openGLDebugEnabled,
                                                    windowWidth,
                                                    windowHeight);
             final var assetManager = new LWJGLAssetManager(assetRoot);
             final var backend = new LWJGLRenderingBackend(assetRoot);
             final var renderer = new RogueliteGameRenderer(runner.getEvents(),
                                                            runner.getTimeManager(),
                                                            assetRoot,
                                                            runner.getWindow(),
                                                            backend,
                                                            assetManager);
        ) {
            final var inputProvider = new LWJGLInputProvider(runner.getWindow());
            runner.run(createInitialGameMode(host, port), inputProvider, renderer);
        }
    }

    private static GameMode createInitialGameMode(
            final String host,
            final int port
    ) {
        // FIXME: Do not pass the host and the port to main menu. Instead, connect and start game if
        //  host is given
        LOG.trace("Creating main menu game state with host and port {}:{}", host, port);
        return MainMenuGameMode.create(host, port);
    }
}
