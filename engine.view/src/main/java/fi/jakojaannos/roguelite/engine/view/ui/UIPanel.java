package fi.jakojaannos.roguelite.engine.view.ui;

import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.sprite.Sprite;
import lombok.Getter;

import java.util.List;

public final class UIPanel<TTexture extends Texture> extends UIElement {
    protected final int borderSize;
    private final Sprite<TTexture> sprite;
    private final SpriteBatch<TTexture> spriteBatch;

    private UIPanel(
            final String name,
            final ElementBoundaries bounds,
            final List<UIElement> children,
            final int borderSize,
            final Sprite<TTexture> sprite,
            final SpriteBatch<TTexture> spriteBatch
    ) {
        super(name, children, bounds);
        this.borderSize = borderSize;
        this.sprite = sprite;
        this.spriteBatch = spriteBatch;
    }

    public static <TTexture extends Texture> Builder<TTexture> builder(final String name) {
        return new Builder<>(name);
    }

    @Override
    public void draw(final int x, final int y, final int width, final int height) {
        this.spriteBatch.begin();

        drawPanelRow(this.sprite, PanelRow.TOP, x, y, width, this.borderSize, this.borderSize);
        drawPanelRow(this.sprite, PanelRow.MIDDLE, x, y + this.borderSize, width, height - 2 * this.borderSize, this.borderSize);
        drawPanelRow(this.sprite, PanelRow.BOTTOM, x, y + height - this.borderSize, width, this.borderSize, this.borderSize);

        this.spriteBatch.end();
    }

    private void drawPanelRow(
            final Sprite<TTexture> sprite,
            final PanelRow row,
            final double x,
            final double y,
            final double width,
            final double height,
            final double borderSize
    ) {
        this.spriteBatch.draw(sprite, row.getLeft(), 0, x, y, borderSize, height);
        this.spriteBatch.draw(sprite, row.getMiddle(), 0, x + borderSize, y, width - 2 * borderSize, height);
        this.spriteBatch.draw(sprite, row.getRight(), 0, x + width - borderSize, y, borderSize, height);
    }

    private enum PanelRow {
        TOP("panel_top_left", "panel_top", "panel_top_right"),
        MIDDLE("panel_left", "panel_fill", "panel_right"),
        BOTTOM("panel_bottom_left", "panel_bottom", "panel_bottom_right");

        @Getter private final String left;
        @Getter private final String middle;
        @Getter private final String right;

        PanelRow(final String left, final String middle, final String right) {
            this.left = left;
            this.middle = middle;
            this.right = right;
        }
    }

    public static final class Builder<TTexture extends Texture>
            extends UIElement.Builder<UIPanel<TTexture>, Builder<TTexture>> {
        private int borderSize = 8;
        private Sprite<TTexture> sprite;
        private SpriteBatch<TTexture> spriteBatch;

        private Builder(final String name) {
            super(name);
        }

        public Builder<TTexture> borderSize(final int borderSize) {
            this.borderSize = borderSize;
            return this;
        }

        @Override
        public UIPanel<TTexture> build() {
            return new UIPanel<>(this.name,
                                 this.bounds,
                                 this.children,
                                 this.borderSize,
                                 this.sprite,
                                 this.spriteBatch);
        }

        public Builder<TTexture> sprite(
                final Sprite<TTexture> sprite,
                final SpriteBatch<TTexture> spriteBatch
        ) {
            this.sprite = sprite;
            this.spriteBatch = spriteBatch;
            return this;
        }
    }
}
