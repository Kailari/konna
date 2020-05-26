package fi.jakojaannos.konna.engine.view.ui.impl;

import fi.jakojaannos.konna.engine.view.ui.UiElement;
import fi.jakojaannos.konna.engine.view.ui.UiUnit;

public record UiPixelUnit(double value) implements UiUnit {
    @Override
    public double calculate(final UiElement element, final double parentValue) {
        // FIXME: Figure out some way of obtaining info about the viewport here. This treats
        //        the value as NDC-space coordinate instead of pixels
        return this.value;
    }
}
