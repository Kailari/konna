package fi.jakojaannos.roguelite.launcher;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * The main entry point. Passes command-line arguments to the launcher and proceeds to launch.
 */
@Slf4j
public class Main {
    public static void main(final String[] args) {
        LOG.trace("Got command-line arguments: {}", Arrays.toString(args));

        final var launcher = new RogueliteLauncher();
        launcher.parseCommandLineArguments(args);
        launcher.launch();
    }
}
