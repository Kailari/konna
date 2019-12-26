package fi.jakojaannos.roguelite.engine.view.ui;

import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;

import java.util.List;

public class UILabel extends UIElement {
    private final String text;
    private final int fontSize;
    private final TextRenderer textRenderer;

    protected UILabel(
            final String name,
            final ElementBoundaries bounds,
            final List<UIElement> children,
            final String text,
            final int fontSize,
            final TextRenderer textRenderer
    ) {
        super(name, children, bounds);
        this.text = text;
        this.fontSize = fontSize;
        this.textRenderer = textRenderer;
    }

    @Override
    protected void draw(final int x, final int y, final int width, final int height) {
        this.textRenderer.drawOnScreen(x, y, this.fontSize, this.text);
    }
}
