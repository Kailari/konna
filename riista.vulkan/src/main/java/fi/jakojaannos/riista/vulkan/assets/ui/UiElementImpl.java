package fi.jakojaannos.riista.vulkan.assets.ui;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import fi.jakojaannos.riista.view.ui.*;

import static java.util.function.Predicate.not;

// Fields may NOT be final here; GSON does not always work properly if things are `final`
@SuppressWarnings("FieldMayBeFinal")
public class UiElementImpl implements UiElement {
    private Collection<UiElementImpl> children = List.of();
    @Nullable private transient UiElementImpl parent;

    private Bounds bounds = new Bounds();
    private Anchor anchor = new Anchor();

    @Nullable private UiText text;
    @Nullable private Color color;

    private String name;

    @Nullable private UiElement hoverElement;

    public void setParent(@Nullable final UiElementImpl parent) {
        this.parent = parent;
    }

    @Override
    public Collection<UiElement> children() {
        return Collections.unmodifiableCollection(this.children);
    }

    public void clearVariantChildren() {
        this.children = this.children.stream()
                                     .filter(not(UiElementImpl::isVariant))
                                     .collect(Collectors.toUnmodifiableList());
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
        this.color = color;
        return this;
    }

    @Override
    public Color color() {
        var element = this;
        while (element != null && element.color == null) {
            element = element.parent;
        }

        return element == null
                ? Colors.TRANSPARENT_BLACK
                : element.color;
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
    @Nullable
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

    @Nullable
    public UiUnit width() {
        return this.bounds.width();
    }

    @Override
    public UiElement height(final UiUnit value) {
        this.bounds.height(value);
        return this;
    }

    @Nullable
    public UiUnit height() {
        return this.bounds.height();
    }

    @Override
    public UiElement text(final String format) {
        if (this.text == null) {
            this.text = new UiText();
        }

        this.text.format(format);
        return this;
    }

    @Override
    public UiElement text(final String format, final String... argKeys) {
        if (this.text == null) {
            this.text = new UiText();
        }

        this.text.format(format);
        this.text.args(argKeys);
        return this;
    }

    @Nullable
    @Override
    public UiText text() {
        return this.text;
    }

    @Override
    public UiUnit anchorX() {
        return this.anchor.x();
    }

    @Override
    public UiUnit anchorY() {
        return this.anchor.y();
    }

    @Override
    public Optional<UiElement> hoverElement() {
        return Optional.ofNullable(this.hoverElement);
    }

    @Override
    public void hoverElement(@Nullable final UiElement element) {
        this.hoverElement = element;
    }

    private static boolean isVariant(final UiElementImpl child) {
        return child.name.startsWith("hover:");
    }
}
