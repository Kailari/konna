package fi.jakojaannos.roguelite.engine.view.data.components.internal.label;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Component;

public class Text implements Component {
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
