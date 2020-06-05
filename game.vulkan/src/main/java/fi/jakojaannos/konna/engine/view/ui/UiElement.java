package fi.jakojaannos.konna.engine.view.ui;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;

@SuppressWarnings("UnusedReturnValue")
public interface UiElement {
    String name();

    /**
     * Gets a child with given name from this element. Only immediate children of this element are queried. If such
     * child is not found, one is created and the given initializer applied on the newly created child. The initializer
     * is executed only if there is no existing child.
     *
     * @param name        name of the child
     * @param initializer initializer to run for newly created child, in case existing child is not found
     *
     * @return the child if one exists. Newly created child element otherwise.
     */
    UiElement getOrCreateChild(String name, Consumer<UiElement> initializer);

    /**
     * Sets the border type and width for given sides.
     *
     * @param sides  sides to apply the changes to
     * @param border the border type
     * @param width  the border width
     *
     * @return self
     */
    UiElement border(Sides sides, Border border, UiUnit width);

    /**
     * Sets the border type and width for all sides.
     *
     * @param border the border type
     * @param width  the border width
     *
     * @return self
     *
     * @see #border(Sides, Border, UiUnit)
     */
    default UiElement border(final Border border, final UiUnit width) {
        return border(Sides.ALL, border, width);
    }

    /**
     * Sets the border width for the given sides.
     *
     * @param sides sides to apply the changes to
     * @param width the border width
     *
     * @return self
     */
    UiElement borderWidth(Sides sides, UiUnit width);

    /**
     * Sets the border width for all sides.
     *
     * @param width the border width
     *
     * @return self
     *
     * @see #borderWidth(Sides, UiUnit)
     */
    default UiElement borderWidth(final UiUnit width) {
        return borderWidth(Sides.ALL, width);
    }

    /**
     * Sets the size of the corner "stubs" for the {@link Border#CORNERS CORNERS}-mode borders.
     *
     * @param sides sides to apply the changes to
     * @param size  the size of the corners
     *
     * @return self
     */
    UiElement borderCornerSize(Sides sides, UiUnit size);

    /**
     * Sets the size of the corner "stubs" for the {@link Border#CORNERS CORNERS}-mode borders, for all sides.
     *
     * @param size the size of the corners
     *
     * @return self
     *
     * @see #borderCornerSize(Sides, UiUnit)
     */
    default UiElement borderCornerSize(final UiUnit size) {
        return borderCornerSize(Sides.ALL, size);
    }

    UiElement color(Color color);

    Color color();

    default UiElement top(final UiUnit value) {
        return offset(Sides.TOP, value);
    }

    @Nullable
    default UiUnit top() {
        return offset(Sides.TOP);
    }

    default UiElement bottom(final UiUnit value) {
        return offset(Sides.BOTTOM, value);
    }

    @Nullable
    default UiUnit bottom() {
        return offset(Sides.BOTTOM);
    }

    default UiElement right(final UiUnit value) {
        return offset(Sides.RIGHT, value);
    }

    @Nullable
    default UiUnit right() {
        return offset(Sides.RIGHT);
    }

    default UiElement left(final UiUnit value) {
        return offset(Sides.LEFT, value);
    }

    @Nullable
    default UiUnit left() {
        return offset(Sides.LEFT);
    }

    /**
     * Sets the offset for the given side. Equivalent of calling e.g. {@link #top(UiUnit) top}.
     *
     * @param sides sides to apply the offset to
     * @param value the offset value
     *
     * @return self
     */
    UiElement offset(Sides sides, UiUnit value);

    @Nullable
    UiUnit offset(Sides side);

    UiElement height(UiUnit value);

    @Nullable
    UiUnit height();

    UiElement width(UiUnit value);

    @Nullable
    UiUnit width();

    UiElement text(String format);

    UiElement text(String format, String... argKeys);

    @Nullable
    UiText text();

    Collection<UiElement> children();

    UiUnit anchorX();

    UiUnit anchorY();

    Optional<UiElement> hoverElement();

    void hoverElement(@Nullable UiElement element);
}
