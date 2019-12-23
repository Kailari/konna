package fi.jakojaannos.roguelite.engine.view.ui;

import java.util.List;

public class UILabel extends UIElement {
    private final String text;
    private final int fontSize;

    protected UILabel(
            final String name,
            final ElementBoundaries bounds,
            final List<UIElement> children,
            final String text,
            final int fontSize
    ) {
        super(name, children, bounds);
        this.text = text;
        this.fontSize = fontSize;
    }

    public static Builder builder(final String name) {
        return new Builder(name);
    }

    @Override
    protected void draw(final int left, final int right, final int top, final int bottom) {

    }

    public static final class Builder extends UIElement.Builder<UILabel, Builder> {
        private String text;
        private int fontSize;

        public Builder(final String name) {
            super(name);
        }

        public Builder text(final String text) {
            this.text = text;
            return this;
        }

        public Builder fontSize(final int fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        @Override
        public UILabel build() {
            return new UILabel(this.name, this.bounds, this.children, this.text, this.fontSize);
        }
    }
}
