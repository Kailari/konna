package fi.jakojaannos.roguelite;

public class Main {
    public static void main(final String[] args) {
        try (final var app = Application.initialize(800, 600)) {
            app.window().show();

            while (app.window().isOpen()) {
                app.window().handleOSEvents();
            }
        }
    }
}
