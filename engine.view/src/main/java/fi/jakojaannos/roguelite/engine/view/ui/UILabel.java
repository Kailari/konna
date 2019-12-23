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

    public static Builder builder(final String name, final TextRenderer textRenderer) {
        return new Builder(name, textRenderer);
    }

    @Override
    protected void draw(final int x, final int y, final int width, final int height) {
        this.textRenderer.drawOnScreen(x, y, this.fontSize, this.text);
    }

    public static final class Builder extends UIElement.Builder<UILabel, Builder> {
        private String text = "";
        private int fontSize = 12;
        private final TextRenderer textRenderer;

        public Builder(
                final String name,
                final TextRenderer textRenderer
        ) {
            super(name);
            this.textRenderer = textRenderer;
        }

        public Builder text(final String text) {
            this.text = text;
            this.bounds.setWidth((int) this.textRenderer.getStringWidthInPixels(this.fontSize, text));
            return this;
        }

        public Builder fontSize(final int fontSize) {
            this.fontSize = fontSize;
            this.bounds.setWidth((int) this.textRenderer.getStringWidthInPixels(this.fontSize, text));
            return this;
        }

        @Override
        public UILabel build() {
            return new UILabel(this.name, this.bounds, this.children, this.text, this.fontSize, this.textRenderer);
        }
    }
}
