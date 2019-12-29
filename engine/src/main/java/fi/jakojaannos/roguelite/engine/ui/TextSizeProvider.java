package fi.jakojaannos.roguelite.engine.ui;

public interface TextSizeProvider {
    double getStringWidthInPixels(int fontSize, String text);

    default double getStringHeightInPixels(int fontSize, String string) {
        return fontSize;
    }
}
