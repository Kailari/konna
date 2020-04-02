package fi.jakojaannos.roguelite.engine.view.data.components.internal;

import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;

public class BoundTop implements ProportionValueComponent {
    public ProportionValue value;

    @Override
    public ProportionValue getValue() {
        return this.value;
    }

    @Override
    public void setValue(final ProportionValue value) {
        this.value = value;
    }

    public BoundTop(final ProportionValue value) {
        this.value = value;
    }
}
