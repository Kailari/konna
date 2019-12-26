package fi.jakojaannos.roguelite.engine.view.text;

public interface TextRenderer {
    double getStringWidthInPixels(int fontSize, String string);

    default double getStringHeightInPixels(int fontSize, String text) {
        return fontSize;
    }

    void drawOnScreen(
            double x,
            double y,
            int fontSize,
            String string
    );
}
