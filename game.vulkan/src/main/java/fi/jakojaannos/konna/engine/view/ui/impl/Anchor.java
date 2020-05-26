package fi.jakojaannos.konna.engine.view.ui.impl;

import fi.jakojaannos.konna.engine.view.ui.UiUnit;

import static fi.jakojaannos.konna.engine.view.ui.UiUnit.percent;

public final class Anchor {
    private UiUnit x = percent(0);
    private UiUnit y = percent(0);

    public UiUnit x() {
        return this.x;
    }

    public void x(final UiUnit value) {
        this.x = value;
    }

    public UiUnit y() {
        return this.y;
    }

    public void y(final UiUnit value) {
        this.y = value;
    }
}
