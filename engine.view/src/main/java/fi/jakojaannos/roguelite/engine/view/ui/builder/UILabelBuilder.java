package fi.jakojaannos.roguelite.engine.view.ui.builder;

import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public class UILabelBuilder extends UIElementBuilder<UILabelBuilder> {
    public UILabelBuilder(
            final UserInterface userInterface,
            final UIElement element,
            final String name
    ) {
        super(userInterface, element, name);
    }

    public UILabelBuilder text(final String text) {
        return property(UIProperty.TEXT, text);
    }

    public UILabelBuilder fontSize(final int value) {
        return property(UIProperty.FONT_SIZE, value);
    }

    @Override
    public UILabelBuilder width(final ProportionValue value) {
        throw new UnsupportedOperationException("Cannot set width of a label. Use fontSize instead.");
    }

    @Override
    public UILabelBuilder height(final ProportionValue value) {
        throw new UnsupportedOperationException("Cannot set height of a label. Use fontSize instead.");
    }
}
