package fi.jakojaannos.roguelite.engine.view.text;

public interface TextRenderer extends AutoCloseable {
    void draw(
            double x,
            double y,
            int fontSize,
            Font font,
            String string
    );

    void drawCentered(double x, double y, int fontSize, Font font, String gameOverMessage);
}
