package fi.jakojaannos.roguelite.engine.view.ui;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

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
}
