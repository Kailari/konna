package fi.jakojaannos.roguelite;

public class Main {
    public static void main(final String[] args) {
        try (final var app = Application.initialize()) {
            while (app.window().isOpen()) {
                app.window().handleOSEvents();
            }
        }
    }
}
