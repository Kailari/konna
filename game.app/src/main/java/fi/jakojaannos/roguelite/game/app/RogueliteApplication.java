package fi.jakojaannos.roguelite.game.app;

import fi.jakojaannos.roguelite.game.DebugConfig;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
public class RogueliteApplication {
    @Setter private boolean debugStackTraces = false;
    @Setter private int windowWidth = -1;
    @Setter private int windowHeight = -1;
    @Setter private String host = "localhost";
    @Setter private int port = 18181;
    @Setter private Path assetRoot;

    public void setDebugMode(boolean state) {
        setDebugStackTraces(state);
        DebugConfig.debugModeEnabled = state;
    }

    public void run(final boolean runAsServer) {
        if (runAsServer) {
            RogueliteServer.run(this.assetRoot,
                                this.port,
                                this.debugStackTraces,
                                this.windowWidth,
                                this.windowHeight);
        } else {
            RogueliteClient.run(this.assetRoot,
                                this.host,
                                this.port,
                                this.debugStackTraces,
                                this.windowWidth,
                                this.windowHeight);
        }
    }
}
