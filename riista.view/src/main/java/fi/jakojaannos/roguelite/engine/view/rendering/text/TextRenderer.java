package fi.jakojaannos.roguelite.engine.view.rendering.text;

public interface TextRenderer extends AutoCloseable {
    default void draw(
            final double x,
            final double y,
            final int fontSize,
            final Font font,
            final String string
    ) {
        draw(x, y, fontSize, font, string, 1.0, 1.0, 1.0);
    }

    void draw(
            double x,
            double y,
            int fontSize,
            Font font,
            String string,
            double r,
            double g,
            double b
    );

    void drawCentered(double x, double y, int fontSize, Font font, String gameOverMessage);
}
