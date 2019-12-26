package fi.jakojaannos.roguelite.engine.view.ui;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import javax.annotation.Nullable;

public class ElementBoundaries {
    @Nullable private Integer x = null, left = null;
    @Nullable private Integer width = null, right = null;
    @Nullable private Integer y = null, top = null;
    @Nullable private Integer height = null, bottom = null;

    @Getter @Setter private double originX, originY;

    public void setX(final int x) {
        this.x = x;
        this.left = null;
    }

    public void setLeft(final int left) {
        this.x = null;
        this.left = left;
    }

    public void setWidth(final int width) {
        this.width = width;
        this.right = null;
    }

    public void setRight(final int right) {
        this.width = null;
        this.right = right;
    }

    public void setY(final int y) {
        this.y = y;
        this.top = null;
    }

    public void setTop(final int top) {
        this.y = null;
        this.top = top;
    }

    public void setHeight(final int height) {
        this.height = height;
        this.bottom = null;
    }

    public void setBottom(final int bottom) {
        this.height = null;
        this.bottom = bottom;
    }

    public void setOffsets(final int left, final int right, final int top, final int bottom) {
        setLeft(left);
        setRight(right);
        setTop(top);
        setBottom(bottom);
    }

    public int getMinX(final int parentX, final int parentWidth) {
        val originOffset = this.x != null ? getWidth(parentWidth) * this.originX : 0;
        return parentX + getMinOffset(this.left, this.x, this.right, this.width) - (int) originOffset;
    }

    public int getWidth(final int parentWidth) {
        return this.width != null
                ? this.width
                : parentWidth - (getMinOffset(this.left, this.x, this.right, this.width) + getMaxOffset(this.right));
    }


    public int getMinY(final int parentY, final int parentHeight) {
        val originOffset = this.y != null ? getHeight(parentHeight) * this.originX : 0;
        return parentY + getMinOffset(this.top, this.y, this.bottom, this.height) - (int) originOffset;
    }

    public int getHeight(final int parentHeight) {
        return this.height != null
                ? this.height
                : parentHeight - (getMinOffset(this.top, this.y, this.bottom, this.height) + getMaxOffset(this.bottom));
    }

    private static int getMinOffset(
            @Nullable final Integer min,
            @Nullable final Integer coordinate,
            @Nullable final Integer max,
            @Nullable final Integer size
    ) {
        if (min == null && coordinate == null) {
            if (max == null || size == null) {
                throw new IllegalStateException("Cannot get left offset! Not enough element size information is defined!");
            }

            return 0;
        }

        return min != null
                ? min
                : coordinate;
    }

    private static int getMaxOffset(@Nullable final Integer max) {
        return max != null ? max : 0;
    }
}
