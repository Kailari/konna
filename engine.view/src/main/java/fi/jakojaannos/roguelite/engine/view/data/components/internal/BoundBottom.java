package fi.jakojaannos.roguelite.engine.view.data.components.internal;

import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;

public class BoundBottom implements ProportionValueComponent {
    public ProportionValue value;

    @Override
    public ProportionValue getValue() {
        return this.value;
    }

    @Override
    public void setValue(final ProportionValue value) {
        this.value = value;
    }

    public BoundBottom(final ProportionValue value) {
        this.value = value;
    }
}
