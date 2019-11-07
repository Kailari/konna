package fi.jakojäännös.launcher;

import fi.jakojäännös.roguelite.engine.lwjgl.LWJGLGameRunner;
import fi.jakojäännös.roguelite.engine.lwjgl.input.LWJGLInputProvider;
import fi.jakojäännös.roguelite.game.Roguelite;
import fi.jakojäännös.roguelite.game.data.GameState;
import fi.jakojäännös.roguelite.game.view.RogueliteGameRenderer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
public class Main {
    public static void main(String[] args) {
        val debugStackTraces = true;
        val enableForceClose = true;

        try (val runner = new LWJGLGameRunner<Roguelite, LWJGLInputProvider, GameState>()) {
            try (val renderer = new RogueliteGameRenderer(runner.getWindow()); val game = new Roguelite()) {
                val inputProvider = new LWJGLInputProvider(runner.getWindow(), enableForceClose);
                runner.run(GameState::new, game, inputProvider, renderer);
            }
        } catch (Exception e) {
            LOG.error("The game loop unexpectedly stopped.");
            LOG.error("\tException:\t{}", e.getClass().getName());
            LOG.error("\tAt:\t\t{}:{}", e.getStackTrace()[0].getFileName(), e.getStackTrace()[0].getLineNumber());
            LOG.error("\tCause:\t\t{}", Optional.ofNullable(e.getCause()).map(Throwable::toString).orElse("Cause not defined."));
            LOG.error("\tMessage:\t{}", e.getMessage());

            if (debugStackTraces) {
                LOG.error("\tStackTrace:\n{}",
                        Arrays.stream(e.getStackTrace())
                                .map(StackTraceElement::toString)
                                .reduce(e.toString(),
                                        (accumulator, element) -> String.format("%s\n\t%s", accumulator, element)));
            } else {
                LOG.error("\tRun with --debug for stack traces");
            }
        }
    }
}
