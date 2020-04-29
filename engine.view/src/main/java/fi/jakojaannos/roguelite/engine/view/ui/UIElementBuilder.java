package fi.jakojaannos.roguelite.engine.view.ui;

import java.util.function.Consumer;

public interface UIElementBuilder {
    <T> UIElementBuilder property(UIProperty<T> property, T value);

    UIElementBuilder child(
            String name,
            Consumer<UIElementBuilder> childBuilder
    );

    default UIElementBuilder anchorX(final ProportionValue value) {
        return property(UIProperty.ANCHOR_X, value);
    }

    default UIElementBuilder anchorY(final ProportionValue value) {
        return property(UIProperty.ANCHOR_Y, value);
    }

    default UIElementBuilder left(final ProportionValue value) {
        return property(UIProperty.LEFT, value);
    }

    default UIElementBuilder right(final ProportionValue value) {
        return property(UIProperty.RIGHT, value);
    }

    default UIElementBuilder width(final ProportionValue value) {
        return property(UIProperty.WIDTH, value);
    }

    default UIElementBuilder top(final ProportionValue value) {
        return property(UIProperty.TOP, value);
    }

    default UIElementBuilder bottom(final ProportionValue value) {
        return property(UIProperty.BOTTOM, value);
    }

    default UIElementBuilder height(final ProportionValue value) {
        return property(UIProperty.HEIGHT, value);
    }

    default UIElementBuilder color(final double r, final double g, final double b) {
        return property(UIProperty.COLOR, new Color(r, g, b));
    }

    default UIElementBuilder text(final String text) {
        return property(UIProperty.TEXT, text);
    }

    default UIElementBuilder fontSize(final int value) {
        return property(UIProperty.FONT_SIZE, value);
    }

    default UIElementBuilder borderSize(final int value) {
        return property(UIProperty.BORDER_SIZE, value);
    }

    default UIElementBuilder sprite(final String sprite) {
        return property(UIProperty.SPRITE, sprite);
    }
}
