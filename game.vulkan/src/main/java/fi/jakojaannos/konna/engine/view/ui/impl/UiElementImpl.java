package fi.jakojaannos.konna.engine.view.ui.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import fi.jakojaannos.konna.engine.view.ui.*;

public class UiElementImpl implements UiElement {
    private Collection<UiElementImpl> children = List.of();
    private Anchor anchor = new Anchor();
    private Bounds bounds = new Bounds();
    private UiText text;

    private String name;

    @Override
    public Collection<UiElement> children() {
        return Collections.unmodifiableCollection(this.children);
    }

    @Override
    public UiElement getOrCreateChild(final String name, final Consumer<UiElement> initializer) {
        return this.children.stream()
                            .filter(child -> child.name().equals(name))
                            .findAny()
                            .orElseGet(() -> {
                                final var child = new UiElementImpl();
                                child.name = name;

                                initializer.accept(child);
                                this.children.add(child);

                                return child;
                            });
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public UiElement border(final Sides sides, final Border border, final UiUnit width) {
        return null;
    }

    @Override
    public UiElement borderWidth(final Sides sides, final UiUnit width) {
        return null;
    }

    @Override
    public UiElement borderCornerSize(final Sides sides, final UiUnit size) {
        return null;
    }

    @Override
    public UiElement color(final Color color) {
        return null;
    }

    @Override
    public UiElement offset(final Sides sides, final UiUnit value) {
        for (final Sides side : sides) {
            switch (side) {
                case TOP -> this.bounds.top(value);
                case BOTTOM -> this.bounds.bottom(value);
                case LEFT -> this.bounds.left(value);
                case RIGHT -> this.bounds.right(value);
            }
        }

        return this;
    }

    @Override
    public UiUnit offset(final Sides side) {
        return switch (side) {
            case TOP -> this.bounds.top();
            case BOTTOM -> this.bounds.bottom();
            case LEFT -> this.bounds.left();
            case RIGHT -> this.bounds.right();
            default -> throw new IllegalArgumentException("Cannot get offset for compound side enumeration! "
                                                          + "Offending value: \"" + side + "\"");
        };
    }

    @Override
    public UiElement width(final UiUnit value) {
        this.bounds.width(value);
        return this;
    }

    @Override
    public UiElement height(final UiUnit value) {
        this.bounds.height(value);
        return this;
    }

    @Override
    public UiElement text(final String format) {
        return null;
    }
}
