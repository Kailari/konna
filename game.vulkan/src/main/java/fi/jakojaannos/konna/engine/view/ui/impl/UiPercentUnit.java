package fi.jakojaannos.konna.engine.view.ui.impl;

import fi.jakojaannos.konna.engine.view.ui.UiElement;
import fi.jakojaannos.konna.engine.view.ui.UiUnit;

public record UiPercentUnit(double value) implements UiUnit {
    @Override
    public double calculate(final UiElement element, final double parentValue, final double framebufferSize) {
        return parentValue * this.value;
    }
}
