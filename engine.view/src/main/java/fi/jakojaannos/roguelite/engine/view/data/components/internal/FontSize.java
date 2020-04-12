package fi.jakojaannos.roguelite.engine.view.data.components.internal;

public class FontSize {
    public int value;

    public int getValue() {
        return this.value;
    }

    public void setValue(final int value) {
        this.value = value;
    }

    public FontSize(final int value) {
        this.value = value;
    }
}
