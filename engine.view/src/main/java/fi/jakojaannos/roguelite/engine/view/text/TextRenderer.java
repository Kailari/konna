package fi.jakojaannos.roguelite.engine.view.text;

public interface TextRenderer {
    double getStringWidthInPixels(int fontSize, String string);

    void drawOnScreen(
            double x,
            double y,
            int fontSize,
            String string
    );
}
