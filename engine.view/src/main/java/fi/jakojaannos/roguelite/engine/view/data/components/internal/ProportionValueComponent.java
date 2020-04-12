package fi.jakojaannos.roguelite.engine.view.data.components.internal;

import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;

public interface ProportionValueComponent {
    ProportionValue getValue();

    void setValue(ProportionValue value);
}
