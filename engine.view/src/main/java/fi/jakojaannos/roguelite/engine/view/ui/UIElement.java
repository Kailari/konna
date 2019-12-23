package fi.jakojaannos.roguelite.engine.view.ui;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class UIElement {
    private final String name;
    private final List<UIElement> children;
    @Getter private final ElementBoundaries bounds;

    public final void render(
            final int parentX,
            final int parentY,
            final int parentWidth,
            final int parentHeight
    ) {
        val x = this.bounds.getMinX(parentX, parentWidth);
        val y = this.bounds.getMinY(parentY, parentHeight);
        val width = this.bounds.getWidth(parentWidth);
        val height = this.bounds.getHeight(parentHeight);
        this.draw(x, y, width, height);
        this.children.forEach(child -> child.render(x, y, width, height));
    }

    protected abstract void draw(int x, int y, int width, int height);

    @Slf4j
    @SuppressWarnings("unchecked")
    @RequiredArgsConstructor
    public static abstract class Builder<T extends UIElement, TBuilder extends Builder<T, TBuilder>> {
        protected final String name;
        protected ElementBoundaries bounds = new ElementBoundaries();
        protected List<UIElement> children = new ArrayList<>();

        public TBuilder offsets(
                final int left,
                final int right,
                final int top,
                final int bottom
        ) {
            bounds.setOffsets(left, right, top, bottom);
            return (TBuilder) this;
        }

        public TBuilder anchor(
                final double anchorX,
                final double anchorY
        ) {
            this.bounds.setAnchorX(anchorX);
            this.bounds.setAnchorY(anchorY);
            return (TBuilder) this;
        }

        public TBuilder origin(final double percentX, final double percentY) {
            this.bounds.setOriginX(percentX);
            this.bounds.setOriginY(percentY);
            return (TBuilder) this;
        }

        public TBuilder size(
                final int width,
                final int height
        ) {
            this.bounds.setWidth(width);
            this.bounds.setHeight(height);
            return (TBuilder) this;
        }

        public TBuilder position(final int x, final int y) {
            this.bounds.setX(x);
            this.bounds.setY(y);
            return (TBuilder) this;
        }

        public TBuilder child(final UIElement element) {
            this.children.add(element);
            return (TBuilder) this;
        }

        public abstract T build();
    }
}
