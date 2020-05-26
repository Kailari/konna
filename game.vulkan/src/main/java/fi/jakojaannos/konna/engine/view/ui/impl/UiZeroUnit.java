package fi.jakojaannos.konna.engine.view.ui.impl;

import fi.jakojaannos.konna.engine.view.ui.UiElement;
import fi.jakojaannos.konna.engine.view.ui.UiUnit;

public class UiZeroUnit implements UiUnit {
    @Override
    public double calculate(final UiElement element, final double parentValue) {
        return 0;
    }
}
