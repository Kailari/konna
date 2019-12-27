package fi.jakojaannos.roguelite.launcher;

import fi.jakojaannos.roguelite.game.app.RogueliteApplication;
import fi.jakojaannos.roguelite.launcher.arguments.Argument;
import fi.jakojaannos.roguelite.launcher.arguments.ArgumentParsingException;
import fi.jakojaannos.roguelite.launcher.arguments.Arguments;
import fi.jakojaannos.roguelite.launcher.arguments.Parameter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.nio.file.Path;

@Slf4j
public class RogueliteLauncher {
    // Master-debug flag, this sets a bunch of default debug settings on
    @Setter private boolean debug;

    @Setter private boolean enableLWJGLDebug = false;
    @Setter private boolean enableLWJGLLibraryLoaderDebug = false;
    @Setter private String assetRoot = "assets/";

    private final RogueliteApplication application = new RogueliteApplication();

    public void parseCommandLineArguments(String... args) {
        try {
            Arguments.builder()
                     .with(Argument.withName("window")
                                   .withAction((params) -> {
                                       val width = params.parameter(Parameter.integer("width")
                                                                             .withMin(1));
                                       val height = params.parameter(Parameter.integer("height")
                                                                              .withMin(1));

                                       this.application.setWindowWidth(width);
                                       this.application.setWindowHeight(height);
                                   }))
                     .with(Argument.withName("width")
                                   .withAction(params -> {
                                       val width = params.parameter(Parameter.integer("width")
                                                                             .withMin(1));
                                       this.application.setWindowWidth(width);
                                   }))
                     .with(Argument.withName("height")
                                   .withAction(params -> {
                                       val height = params.parameter(Parameter.integer("height")
                                                                              .withMin(1));
                                       this.application.setWindowHeight(height);
                                   }))
                     .with(Argument.withName("assetRoot")
                                   .withAction(params -> {
                                       val assetRoot = params.parameter(Parameter.filePath("path")
                                                                                 .mustBeDirectory()
                                                                                 .mustExist());

                                       LOG.debug("Changing asset root");
                                       this.setAssetRoot(assetRoot);
                                   }))
                     .with(Argument.withName("enableLWJGLDebug")
                                   .withAction(params -> this.setEnableLWJGLDebug(true)))
                     .with(Argument.withName("enableLWJGLLibraryLoaderDebug")
                                   .withAction(params -> this.setEnableLWJGLLibraryLoaderDebug(true)))
                     .with(Argument.withName("enableForceClose")
                                   .withAction(params -> this.application.setEnableForceClose(true)))
                     .with(Argument.withName("debugStackTraces")
                                   .withAction(params -> this.application.setDebugStackTraces(true)))
                     .with(Argument.withName("debug")
                                   .withAction(params -> this.setDebug(true)))
                     .ignoreUnknown()
                     .consume(args);
        } catch (ArgumentParsingException e) {
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

        this.application.run(Path.of(this.assetRoot));
    }
}
