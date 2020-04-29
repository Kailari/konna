package fi.jakojaannos.roguelite.engine.view.ui.builder;

import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.view.ui.Color;
import fi.jakojaannos.roguelite.engine.view.ui.*;

@SuppressWarnings("unchecked")
public class UIElementBuilder<TBuilder extends UIElementBuilder<TBuilder>> {
    final UIElement element;
    private final UserInterface userInterface;

    public UIElementBuilder(
            final UserInterface userInterface,
            final UIElement element,
            final String name
    ) {
        this.userInterface = userInterface;
        this.element = element;

        this.element.setProperty(UIProperty.NAME, name);
    }

    public TBuilder anchorX(final ProportionValue value) {
        return property(UIProperty.ANCHOR_X, value);
    }

    public TBuilder anchorY(final ProportionValue value) {
        return property(UIProperty.ANCHOR_Y, value);
    }

    public TBuilder left(final ProportionValue value) {
        return property(UIProperty.LEFT, value);
    }

    public TBuilder right(final ProportionValue value) {
        return property(UIProperty.RIGHT, value);
    }

    public TBuilder width(final ProportionValue value) {
        return property(UIProperty.WIDTH, value);
    }

    public TBuilder top(final ProportionValue value) {
        return property(UIProperty.TOP, value);
    }

    public TBuilder bottom(final ProportionValue value) {
        return property(UIProperty.BOTTOM, value);
    }

    public TBuilder height(final ProportionValue value) {
        return property(UIProperty.HEIGHT, value);
    }

    public TBuilder color(final double r, final double g, final double b) {
        return property(UIProperty.COLOR, new Color(r, g, b));
    }

    public <T> TBuilder property(final UIProperty<T> property, final T value) {
        this.element.setProperty(property, value);
        return (TBuilder) this;
    }

    public <TChildElement extends UIElementType<TChildBuilder>, TChildBuilder extends UIElementBuilder<TChildBuilder>>
    TBuilder child(
            final String name,
            final TChildElement childType,
            final Consumer<TChildBuilder> childBuilder
    ) {
        final var child = this.userInterface.addElement(name, childType, childBuilder);
        child.setParent(this.element);
        return (TBuilder) this;
    }
}
