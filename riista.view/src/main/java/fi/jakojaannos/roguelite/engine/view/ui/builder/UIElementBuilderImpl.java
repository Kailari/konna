package fi.jakojaannos.roguelite.engine.view.ui.builder;

import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementBuilder;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public class UIElementBuilderImpl implements UIElementBuilder {
    final UIElement element;
    private final UserInterface userInterface;

    public UIElementBuilderImpl(
            final UserInterface userInterface,
            final UIElement element,
            final String name
    ) {
        this.userInterface = userInterface;
        this.element = element;

        this.element.setProperty(UIProperty.NAME, name);
    }

    @Override
    public <T> UIElementBuilderImpl property(final UIProperty<T> property, final T value) {
        this.element.setProperty(property, value);
        return this;
    }

    @Override
    public UIElementBuilderImpl child(final String name, final Consumer<UIElementBuilder> childBuilder) {
        final var child = this.userInterface.addElement(name, childBuilder);
        child.setParent(this.element);
        return this;
    }
}
