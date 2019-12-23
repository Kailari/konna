package fi.jakojaannos.roguelite.engine.view.ui;

import fi.jakojaannos.roguelite.engine.view.LogCategories;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class UIElement {
    private final String name;
    private final List<UIElement> children;
    private SizeConstraints sizeConstraints;

    protected void updateSize(final SizeConstraints sizeConstraints) {
        this.sizeConstraints = sizeConstraints;
    }

    public final void render(
            final int parentX,
            final int parentY,
            final int parentWidth,
            final int parentHeight
    ) {
        this.sizeConstraints.render(parentX, parentY,
                                    parentWidth, parentHeight,
                                    (x, y, width, height) -> {
                                        this.draw(x, y, width, height);
                                        this.children.forEach(child -> child.render(x, y, width, height));
                                    });
    }

    protected abstract void draw(int x, int y, int width, int height);

    public static abstract class SizeConstraints {
        abstract void render(
                int parentX,
                int parentY,
                int parentWidth,
                int parentHeight,
                RenderFunction renderFunction
        );

        public static SizeConstraints anchored(
                final double anchorX,
                final double anchorY,
                final double originX,
                final double originY,
                final int x,
                final int y,
                final int width,
                final int height
        ) {
            return new Anchored(anchorX, anchorY, originX, originY, x, y, width, height);
        }

        public static SizeConstraints offsets(
                final int left,
                final int right,
                final int top,
                final int bottom
        ) {
            return new Offsets(left, right, top, bottom);
        }

        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        private static class Offsets extends SizeConstraints {
            private final int left, right, top, bottom;

            @Override
            void render(
                    final int parentX,
                    final int parentY,
                    final int parentWidth,
                    final int parentHeight,
                    final RenderFunction renderFunction
            ) {
                val thisLeftX = parentX + this.left;
                val parentRightX = parentX + parentWidth;
                val thisRightX = parentRightX - this.right;
                val thisWidth = thisRightX - thisLeftX;

                val thisTopY = parentY + this.top;
                val parentBottomY = parentY + parentHeight;
                val thisBottomY = parentBottomY - this.bottom;
                val thisHeight = thisBottomY - thisTopY;
                renderFunction.accept(thisLeftX, thisTopY, thisWidth, thisHeight);
            }
        }

        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        private static class Anchored extends SizeConstraints {
            private final double anchorX, anchorY;
            private final double originX, originY;
            private final int x, y;
            private final int width, height;

            @Override
            void render(
                    final int parentX,
                    final int parentY,
                    final int parentWidth,
                    final int parentHeight,
                    final RenderFunction renderFunction
            ) {
                val anchoredX = parentX + (int) Math.round(parentWidth * this.anchorX);
                val anchoredY = parentY + (int) Math.round(parentHeight * this.anchorY);

                val originOffsetX = (int) Math.round(this.originX * this.width);
                val originOffsetY = (int) Math.round(this.originY * this.height);
                renderFunction.accept(anchoredX + this.x - originOffsetX,
                                      anchoredY + this.y - originOffsetY,
                                      this.width,
                                      this.height);
            }
        }
    }

    private interface RenderFunction {
        void accept(int x, int y, int width, int height);
    }

    @Slf4j
    @SuppressWarnings("unchecked")
    @RequiredArgsConstructor
    public static abstract class Builder<T extends UIElement, TBuilder extends Builder<T, TBuilder>> {
        protected final String name;

        protected boolean hasAnchor = false;
        protected double anchorX, anchorY;
        protected double originX, originY;
        protected int x;
        protected int y;
        protected int width;
        protected int height;

        protected boolean hasOffsets = false;
        protected int left, right, top, bottom;

        protected List<UIElement> children = new ArrayList<>();

        public TBuilder offsets(
                final int left,
                final int right,
                final int top,
                final int bottom
        ) {
            ensureDoesNotHaveAnchor();
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
            this.hasOffsets = true;
            return (TBuilder) this;
        }

        public TBuilder anchor(
                final double anchorX,
                final double anchorY
        ) {
            ensureDoesNotHaveOffsets("anchor");
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            this.hasAnchor = true;
            return (TBuilder) this;
        }

        public TBuilder origin(final double percentX, final double percentY) {
            ensureDoesNotHaveOffsets("origin");
            this.originX = percentX;
            this.originY = percentY;
            this.hasAnchor = true;
            return (TBuilder) this;
        }

        public TBuilder size(
                final int width,
                final int height
        ) {
            ensureDoesNotHaveOffsets("size");
            this.width = width;
            this.height = height;
            this.hasAnchor = true;
            return (TBuilder) this;
        }

        public TBuilder position(final int x, final int y) {
            ensureDoesNotHaveOffsets("position");
            this.x = x;
            this.y = y;
            this.hasAnchor = true;
            return (TBuilder) this;
        }

        public TBuilder child(final UIElement element) {
            this.children.add(element);
            return (TBuilder) this;
        }

        protected SizeConstraints buildSizeConstraints() {
            if (this.hasAnchor) {
                return SizeConstraints.anchored(this.anchorX, this.anchorY, this.originX, this.originY, this.x, this.y, this.width, this.height);
            } else if (this.hasOffsets) {
                return SizeConstraints.offsets(this.left, this.right, this.top, this.bottom);
            } else {
                LOG.warn(LogCategories.UI_BUILDER, "Element \"{}\" did not define any size constraints!", this.name);
                return SizeConstraints.offsets(0, 0, 0, 0);
            }
        }

        protected void ensureDoesNotHaveAnchor() {
            if (this.hasAnchor) {
                throw new IllegalStateException("Tried to set offsets of ui element \"" + this.name + "\", but anchor/position was already defined");
            }
        }

        protected void ensureDoesNotHaveOffsets(final String property) {
            if (this.hasOffsets) {
                throw new IllegalStateException("Tried to set " + property + " of ui element \"" + this.name + "\", but offsets were already defined");
            }
        }

        public abstract T build();
    }
}
