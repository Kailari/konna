package fi.jakojaannos.roguelite.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.game.app.RogueliteApplication;
import fi.jakojaannos.roguelite.launcher.arguments.Argument;
import fi.jakojaannos.roguelite.launcher.arguments.ArgumentParsingException;
import fi.jakojaannos.roguelite.launcher.arguments.Arguments;
import fi.jakojaannos.roguelite.launcher.arguments.parameters.Parameter;

public class RogueliteLauncher {
    private static final Logger LOG = LoggerFactory.getLogger(RogueliteLauncher.class);

    private final RogueliteApplication application = new RogueliteApplication();
    /** Master-debug flag, this sets a bunch of default debug settings on */
    private boolean debug;
    private boolean enableLWJGLDebug;
    private boolean enableLWJGLLibraryLoaderDebug;
    private String assetRoot = "assets/";
    private boolean runAsServer;

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    public void setEnableLWJGLDebug(final boolean enableLWJGLDebug) {
        this.enableLWJGLDebug = enableLWJGLDebug;
    }

    public void setEnableLWJGLLibraryLoaderDebug(final boolean enableLWJGLLibraryLoaderDebug) {
        this.enableLWJGLLibraryLoaderDebug = enableLWJGLLibraryLoaderDebug;
    }

    public void setAssetRoot(final String assetRoot) {
        this.assetRoot = assetRoot;
    }

    public void parseCommandLineArguments(final String... args) {
        try {
            Arguments.builder()
                     .with(Argument.withName("server")
                                   .withAction(params -> {
                                       final var port =
                                               params.parameter(Parameter.optional(Parameter.integer("port")));
                                       this.runAsServer = true;
                                       this.application.setPort(port.orElse(18181));
                                   }))
                     .with(Argument.withName("client")
                                   .withAction(params -> {
                                       final var host = params.parameter(Parameter.string("host"));
                                       final var maybePort =
                                               params.parameter(Parameter.optional(Parameter.integer("port")));
                                       this.runAsServer = false;
                                       this.application.setHost(host);
                                       maybePort.ifPresent(this.application::setPort);
                                   }))
                     .with(Argument.withName("window")
                                   .withAction((params) -> {
                                       final var width = params.parameter(Parameter.integer("width")
                                                                                   .withMin(1));
                                       final var height = params.parameter(Parameter.integer("height")
                                                                                    .withMin(1));

                                       this.application.setWindowWidth(width);
                                       this.application.setWindowHeight(height);
                                   }))
                     .with(Argument.withName("width")
                                   .withAction(params -> {
                                       final var width = params.parameter(Parameter.integer("width")
                                                                                   .withMin(1));
                                       this.application.setWindowWidth(width);
                                   }))
                     .with(Argument.withName("height")
                                   .withAction(params -> {
                                       final var height = params.parameter(Parameter.integer("height")
                                                                                    .withMin(1));
                                       this.application.setWindowHeight(height);
                                   }))
                     .with(Argument.withName("assetRoot")
                                   .withAction(params -> {
                                       final var assetRoot = params.parameter(Parameter.filePath("path")
                                                                                       .mustBeDirectory()
                                                                                       .mustExist());

                                       LOG.debug("Changing asset root");
                                       this.setAssetRoot(assetRoot);
                                   }))
                     .with(Argument.withName("enableLWJGLDebug")
                                   .withAction(params -> this.setEnableLWJGLDebug(true)))
                     .with(Argument.withName("enableLWJGLLibraryLoaderDebug")
                                   .withAction(params -> this.setEnableLWJGLLibraryLoaderDebug(true)))
                     .with(Argument.withName("debugStackTraces")
                                   .withAction(params -> this.application.setDebugStackTraces(true)))
                     .with(Argument.withName("debug")
                                   .withAction(params -> this.setDebug(true)))
                     .ignoreUnknown()
                     .consume(args);
        } catch (final ArgumentParsingException e) {
            LOG.error("Error parsing command-line arguments: ", e);
        }

    }

    public void launch() {
        if (this.debug) {
            this.application.setDebugMode(true);
        }

        if (!(this.assetRoot.endsWith("/") || this.assetRoot.endsWith("\\"))) {
            this.assetRoot = this.assetRoot + '/';
        }

        if (this.enableLWJGLDebug) {
            LOG.info("Enabling LWJGL Debug mode");
            System.setProperty("org.lwjgl.util.Debug", "true");
        }

        if (this.enableLWJGLLibraryLoaderDebug) {
            LOG.info("Enabling LWJGL SharedLibraryLoader debug mode");
            System.setProperty("org.lwjgl.util.DebugLoader", "true");
        }

        this.application.setAssetRoot(Path.of(this.assetRoot));
        this.application.run(this.runAsServer);
    }
}
