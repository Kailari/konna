package fi.jakojaannos.konna.engine.view.ui;

import java.util.function.Consumer;

@SuppressWarnings("UnusedReturnValue")
public interface UiElement {
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
    UiElement getOrCreate(String name, Consumer<UiElement> initializer);

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

    UiElement top(UiUnit value);

    UiElement bottom(UiUnit value);

    UiElement right(UiUnit value);

    UiElement left(UiUnit value);

    /**
     * Sets the offset for the given side. Equivalent of calling e.g. {@link #top(UiUnit) top}.
     *
     * @param sides sides to apply the offset to
     * @param value the offset value
     *
     * @return self
     */
    default UiElement offset(final Sides sides, final UiUnit value) {
        for (final var side : sides) {
            switch (side) {
                case LEFT -> left(value);
                case RIGHT -> right(value);
                case TOP -> top(value);
                case BOTTOM -> bottom(value);
            }
        }

        return this;
    }

    UiElement height(UiUnit value);

    UiElement width(UiUnit value);
}
