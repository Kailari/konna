package fi.jakojaannos.roguelite.game.app;

import fi.jakojaannos.roguelite.engine.Game;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLAssetManager;
import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLGameRunner;
import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLRenderingBackend;
import fi.jakojaannos.roguelite.engine.lwjgl.input.LWJGLInputProvider;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.game.DebugConfig;
import fi.jakojaannos.roguelite.game.RogueliteGame;
import fi.jakojaannos.roguelite.game.state.MainMenuGameState;
import fi.jakojaannos.roguelite.game.view.RogueliteGameRenderer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.nio.file.Path;

@Slf4j
public class RogueliteClient {
    public static void run(
            final Path assetRoot,
            final String host,
            final int port,
            final int windowWidth,
            final int windowHeight
    ) throws Exception {
        LOG.trace("Running application");
        LOG.debug("asset root: {}", assetRoot);

        try (val runner = new LWJGLGameRunner<RogueliteGame>(DebugConfig.debugModeEnabled, windowWidth, windowHeight);
             val assetManager = new LWJGLAssetManager(assetRoot);
             val backend = new LWJGLRenderingBackend(assetRoot);
             val renderer = new RogueliteGameRenderer(assetRoot, runner.getWindow(), backend, assetManager);
             val game = new RogueliteGame()
        ) {
            val inputProvider = new LWJGLInputProvider(runner.getWindow());
            runner.run(() -> createInitialState(game, host, port), game, inputProvider, renderer::render);
        }
    }

    private static GameState createInitialState(
            final Game game,
            final String host,
            final int port
    ) {
        // FIXME: Do not pass the host and the port to main menu. Instead, connect and start game if
        //  host is given
        LOG.trace("Creating main menu game state with host and port {}:{}", host, port);
        return new MainMenuGameState(World.createNew(EntityManager.createNew(256, 32)),
                                     game.getTime(), host, port);
    }
}
