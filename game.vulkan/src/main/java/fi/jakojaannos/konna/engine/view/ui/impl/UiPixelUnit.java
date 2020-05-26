package fi.jakojaannos.konna.engine.view.ui.impl;

import fi.jakojaannos.konna.engine.view.ui.UiElement;
import fi.jakojaannos.konna.engine.view.ui.UiUnit;

public record UiPixelUnit(double value) implements UiUnit {
    @Override
    public double calculate(final UiElement element, final double parentValue, final double framebufferSize) {
        // FIXME: Figure out some better way of obtaining info about the viewport here (?)
        return this.value / (framebufferSize / 2.0);
    }
}
