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
import fi.jakojaannos.roguelite.game.network.client.ClientNetworkManager;
import fi.jakojaannos.roguelite.game.state.MainMenuGameState;
import fi.jakojaannos.roguelite.game.view.RogueliteGameRenderer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
public class RogueliteApplication {
    @Setter private boolean debugStackTraces = false;
    @Setter private int windowWidth = -1;
    @Setter private int windowHeight = -1;

    public void setDebugMode(boolean state) {
        setDebugStackTraces(state);
        DebugConfig.debugModeEnabled = state;
    }

    public void run(final Path assetRoot) {
        LOG.trace("Running application");
        LOG.debug("asset root: {}", assetRoot);

        try (val runner = new LWJGLGameRunner<RogueliteGame, LWJGLInputProvider>(DebugConfig.debugModeEnabled, this.windowWidth, this.windowHeight);
             val networkManager = new ClientNetworkManager();
             val assetManager = new LWJGLAssetManager(assetRoot);
             val backend = new LWJGLRenderingBackend(assetRoot);
             val renderer = new RogueliteGameRenderer(assetRoot, runner.getWindow(), backend, assetManager);
             RogueliteGame game = new RogueliteGame(networkManager)
        ) {
            val inputProvider = new LWJGLInputProvider(runner.getWindow());
            // FIXME: Don't do this here, create a main menu button or sth. ":D"
            networkManager.connect("localhost", 18181);
            runner.run(() -> createInitialState(game), game, inputProvider, renderer::render);
        } catch (Exception e) {
            LOG.error("The game loop unexpectedly stopped.");
            LOG.error("\tException:\t{}", e.getClass().getName());
            LOG.error("\tAt:\t\t{}:{}", e.getStackTrace()[0].getFileName(), e.getStackTrace()[0].getLineNumber());
            LOG.error("\tCause:\t\t{}", Optional.ofNullable(e.getCause()).map(Throwable::toString).orElse("Cause not defined."));
            LOG.error("\tMessage:\t{}", e.getMessage());

            if (this.debugStackTraces) {
                LOG.error("\tStackTrace:\n{}",
                          Arrays.stream(e.getStackTrace())
                                .map(StackTraceElement::toString)
                                .reduce(e.toString(),
                                        (accumulator, element) -> String.format("%s\n\t%s", accumulator, element)));
            } else {
                LOG.error("\tRun with --debugStackTraces for stack traces");
            }
        }
    }

    private GameState createInitialState(final Game game) {
        return new MainMenuGameState(World.createNew(EntityManager.createNew(256, 32)),
                                     game.getTime()
        );
    }
}
