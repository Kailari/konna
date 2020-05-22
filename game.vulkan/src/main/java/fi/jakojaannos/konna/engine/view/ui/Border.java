package fi.jakojaannos.konna.engine.view.ui;

/**
 * UI element border type. Instructs the UI element to render with specified border type.
 */
public enum Border {
    /**
     * Full border, the border is rendered as a solid line. Line width is specified by {@link
     * UiElement#borderWidth(Sides, UiUnit) border width}
     */
    FULL,

    /**
     * Borders are rendered only at corners. Stub length at the corner is specified by {@link
     * UiElement#borderCornerSize(Sides, UiUnit) border corner size}.
     */
    CORNERS,

    /** Borders should not be rendered. */
    NONE
}
