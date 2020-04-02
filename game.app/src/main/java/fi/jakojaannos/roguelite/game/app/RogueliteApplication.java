package fi.jakojaannos.roguelite.game.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

import fi.jakojaannos.roguelite.game.DebugConfig;

public class RogueliteApplication {
    private static final Logger LOG = LoggerFactory.getLogger(RogueliteApplication.class);

    private boolean debugStackTraces;
    private int windowWidth = -1;
    private int windowHeight = -1;
    private String host = "localhost";
    private int port = 18181;
    private Path assetRoot;

    public void setDebugStackTraces(final boolean debugStackTraces) {
        this.debugStackTraces = debugStackTraces;
    }

    public void setWindowWidth(final int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public void setWindowHeight(final int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public void setAssetRoot(final Path assetRoot) {
        this.assetRoot = assetRoot;
    }

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
