package fi.jakojaannos.roguelite;

import java.nio.file.Path;

public class Main {
    public static void main(final String[] args) {
        try (final var app = Application.initialize(800,
                                                    600,
                                                    Path.of("assets"));
             final var runner = new ApplicationRunner(app)
        ) {
            runner.run();
        }
    }
}
