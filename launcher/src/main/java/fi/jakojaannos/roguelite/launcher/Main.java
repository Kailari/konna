package fi.jakojaannos.roguelite.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * The main entry point. Passes command-line arguments to the launcher and proceeds to launch.
 */
public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) {
        LOG.trace("Got command-line arguments: {}", Arrays.toString(args));

        final var launcher = new RogueliteLauncher();
        launcher.parseCommandLineArguments(args);
        launcher.launch();
    }
}
