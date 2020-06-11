package fi.jakojaannos.roguelite.game.data.components;

import org.joml.Vector2d;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.roguelite.engine.utilities.math.RotatedRectangle;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;

/**
 * Defines entity collision boundaries and which other entities it should interact with. Each
 * collider belongs to a single {@link CollisionLayer}. The layer defines which other layers it
 * collides or overlaps with.
 * <p>
 * If a layer <strong>collides</strong> with another layer, all colliders belonging to the first
 * layer are blocked from collision boundaries of the second layer. Similarly, if layer is set to
 * <strong>overlap</strong> with another layer, it receives an overlap-event every time a collider
 * belonging to the other layer overlaps with its collision boundaries.
 * <p>
 * In other words: Each layer defines separately which other layers it treats as solid and from
 * which layers it wants overlap events.
 */
public class Collider implements Shape {
    private static final RotatedRectangle tmpBounds = new RotatedRectangle();
    public final Vector2d origin = new Vector2d();
    private final transient Vector2d[] vertices = new Vector2d[]{
            new Vector2d(), new Vector2d(), new Vector2d(), new Vector2d()
    };
    public double width;
    public double height;
    public CollisionLayer layer;
    private transient double lastRotation = Double.NaN;

    public Collider(final CollisionLayer layer) {
        this(layer, 1.0);
    }

    public Collider(final CollisionLayer layer, final double size) {
        this(layer, size, size, 0.0, 0.0);
    }

    public Collider(
            final CollisionLayer layer,
            final double width,
            final double height,
            final double originX,
            final double originY
    ) {
        this.layer = layer;
        this.width = width;
        this.height = height;
        this.origin.set(originX, originY);
    }

    @Override
    public Vector2d[] getVerticesInLocalSpace(final Transform transform) {
        if (this.lastRotation != transform.rotation) {
            this.lastRotation = transform.rotation;

            tmpBounds.set(new Vector2d(0.0), this.origin, this.width, this.height, transform.rotation);
            tmpBounds.getTopLeft(this.vertices[0]);
            tmpBounds.getTopRight(this.vertices[1]);
            tmpBounds.getBottomLeft(this.vertices[2]);
            tmpBounds.getBottomRight(this.vertices[3]);
        }

        return this.vertices;
    }
}
