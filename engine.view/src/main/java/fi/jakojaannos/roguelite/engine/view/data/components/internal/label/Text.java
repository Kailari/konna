package fi.jakojaannos.roguelite.engine.view.data.components.internal.label;

public class Text {
    private String text;

    public String getText() {
        return this.text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public Text(final String text) {
        this.text = text;
    }
}
