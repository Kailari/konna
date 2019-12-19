package fi.jakojaannos.roguelite.game.app;

import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLGameRunner;
import fi.jakojaannos.roguelite.engine.lwjgl.input.LWJGLInputProvider;
import fi.jakojaannos.roguelite.game.DebugConfig;
import fi.jakojaannos.roguelite.game.Roguelite;
import fi.jakojaannos.roguelite.game.view.RogueliteGameRenderer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
public class RogueliteApplication {
    @Setter private boolean enableForceClose = false;
    @Setter private boolean debugStackTraces = false;
    @Setter private int windowWidth = -1;
    @Setter private int windowHeight = -1;
    @Setter private boolean floatWindow = false;

    public void setDebugMode(boolean state) {
        setEnableForceClose(state);
        setDebugStackTraces(state);
        DebugConfig.debugModeEnabled = state;
    }

    public void run(final Path assetRoot) {
        try (val runner = new LWJGLGameRunner<Roguelite, LWJGLInputProvider>(this.windowWidth, this.windowHeight, this.floatWindow)
        ) {
            try (val renderer = new RogueliteGameRenderer(assetRoot,
                                                          runner.getWindow());
                 val game = new Roguelite()
            ) {
                val inputProvider = new LWJGLInputProvider(runner.getWindow(), this.enableForceClose);
                runner.run(game::createInitialState, game, inputProvider, renderer::render);
            }
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
}
