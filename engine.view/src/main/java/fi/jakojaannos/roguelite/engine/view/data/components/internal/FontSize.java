package fi.jakojaannos.roguelite.engine.view.data.components.internal;

import fi.jakojaannos.roguelite.engine.ecs.Component;

public class FontSize implements Component {
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
