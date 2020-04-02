package fi.jakojaannos.roguelite.engine.view.data.components.ui;

import org.joml.Vector2i;

import fi.jakojaannos.roguelite.engine.ecs.Component;

/**
 * Cached read-only view of a component's boundaries.
 * <p>
 * Note that while the component is not immutable, modifying these fields does nothing (but totally messes up the rest
 * of the UI rendering tick). To actually modify the ui element, use one of the
 * <code>BoundXXX</code> components.
 */
public class ElementBoundaries implements Component {
    public static int INVALID_VALUE = Integer.MIN_VALUE;

    public int minX;
    public int maxX;
    public int minY;
    public int maxY;
    public int width;
    public int height;

    public int getMinX() {
        return this.minX;
    }

    public void setMinX(final int minX) {
        this.minX = minX;
    }

    public int getMaxX() {
        return this.maxX;
    }

    public void setMaxX(final int maxX) {
        this.maxX = maxX;
    }

    public int getMinY() {
        return this.minY;
    }

    public void setMinY(final int minY) {
        this.minY = minY;
    }

    public int getMaxY() {
        return this.maxY;
    }

    public void setMaxY(final int maxY) {
        this.maxY = maxY;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(final int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(final int height) {
        this.height = height;
    }

    public void invalidate() {
        this.minX = ElementBoundaries.INVALID_VALUE;
        this.maxX = ElementBoundaries.INVALID_VALUE;
        this.minY = ElementBoundaries.INVALID_VALUE;
        this.maxY = ElementBoundaries.INVALID_VALUE;
        this.width = ElementBoundaries.INVALID_VALUE;
        this.height = ElementBoundaries.INVALID_VALUE;
    }

    public Vector2i getCenter() {
        return new Vector2i((int) ((this.minX + this.maxX) * 0.5),
                            (int) ((this.minY + this.maxY) * 0.5));
    }
}
