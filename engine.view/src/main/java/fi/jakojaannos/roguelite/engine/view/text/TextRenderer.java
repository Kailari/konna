package fi.jakojaannos.roguelite.engine.view.text;

public interface TextRenderer {
    void drawOnScreen(
            double x,
            double y,
            int fontSize,
            Font font,
            String string
    );
}
