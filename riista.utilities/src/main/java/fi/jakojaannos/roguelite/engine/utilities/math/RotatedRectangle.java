package fi.jakojaannos.roguelite.engine.utilities.math;

import org.joml.Vector2d;

public final class RotatedRectangle {
    public double width;
    public double height;
    public Vector2d originOffset;
    public Vector2d position;
    public double rotation;

    public RotatedRectangle() {
        this(new Vector2d(0.0, 0.0),
             new Vector2d(0.0, 0.0),
             1.0,
             1.0,
             0.0);
    }

    public RotatedRectangle(
            final Vector2d position,
            final Vector2d originOffset,
            final double width,
            final double height,
            final double rotation
    ) {
        this.originOffset = new Vector2d();
        this.position = new Vector2d();
        set(position, originOffset, width, height, rotation);
    }

    public void set(
            final Vector2d position,
            final Vector2d originOffset,
            final double width,
            final double height,
            final double rotation
    ) {
        set(position.x, position.y, originOffset.x, originOffset.y, width, height, rotation);
    }

    public void set(
            final double x,
            final double y,
            final double originX,
            final double originY,
            final double width,
            final double height,
            final double rotation
    ) {
        this.position.set(x, y);
        this.originOffset.set(originX, originY);
        this.width = width;
        this.height = height;
        this.rotation = rotation;
    }

    public Vector2d getTopLeft(final Vector2d result) {
        return getRelative(this.position.x,
                           this.position.y,
                           result);
    }

    public Vector2d getTopRight(final Vector2d result) {
        return getRelative(this.position.x + this.width,
                           this.position.y,
                           result);
    }

    public Vector2d getBottomLeft(final Vector2d result) {
        return getRelative(this.position.x,
                           this.position.y + this.height,
                           result);
    }

    public Vector2d getBottomRight(final Vector2d result) {
        return getRelative(this.position.x + this.width,
                           this.position.y + this.height,
                           result);
    }

    public Vector2d getRelative(final double x, final double y, final Vector2d result) {
        return CoordinateHelper.transformCoordinate(this.position.x,
                                                    this.position.y,
                                                    this.rotation,
                                                    x - this.originOffset.x,
                                                    y - this.originOffset.y,
                                                    result);
    }

}
