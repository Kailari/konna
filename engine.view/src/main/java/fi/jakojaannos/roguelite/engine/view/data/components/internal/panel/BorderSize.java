package fi.jakojaannos.roguelite.engine.view.data.components.internal.panel;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Component;

public class BorderSize implements Component {
    private int value;

    public int getValue() {
        return this.value;
    }

    public void setValue(final int value) {
        this.value = value;
    }

    public BorderSize(final int value) {
        this.value = value;
    }
}
