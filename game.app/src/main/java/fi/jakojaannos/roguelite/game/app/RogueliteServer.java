package fi.jakojaannos.roguelite.game.app;

import java.nio.file.Path;

public class RogueliteServer {
    public static void run(
            final Path assetRoot,
            final int port,
            final boolean debugStackTraces,
            final int windowWidth,
            final int windowHeight
    ) {
        try {
            fi.jakojaannos.roguelite.game.network.RogueliteServer.run(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
