package fi.jakojaannos.roguelite.game.app;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

import fi.jakojaannos.roguelite.game.DebugConfig;

@Slf4j
public class RogueliteApplication {
    @Setter private boolean debugStackTraces;
    @Setter private int windowWidth = -1;
    @Setter private int windowHeight = -1;
    @Setter private String host = "localhost";
    @Setter private int port = 18181;
    @Setter private Path assetRoot;

    public void setDebugMode(final boolean state) {
        setDebugStackTraces(state);
        DebugConfig.debugModeEnabled = state;
    }

    public void run(final boolean runAsServer) {
        try {

            if (runAsServer) {
                RogueliteServer.run(this.port);
            } else {
                RogueliteClient.run(this.assetRoot,
                                    this.host,
                                    this.port,
                                    this.windowWidth,
                                    this.windowHeight);
            }
        } catch (final Exception e) {
            LOG.error("The game loop unexpectedly stopped.");
            LOG.error("\tException:\t{}", e.getClass().getName());
            LOG.error("\tAt:\t\t{}:{}", e.getStackTrace()[0].getFileName(), e.getStackTrace()[0].getLineNumber());
            LOG.error("\tCause:\t\t{}", Optional.ofNullable(e.getCause())
                                                .map(Throwable::toString)
                                                .orElse("Cause not defined."));
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
