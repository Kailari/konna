package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.utilities.math.RotatedRectangle;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector2d;

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
@Slf4j
public class Collider implements Component, Shape {
    public double width;
    public double height;
    public CollisionLayer layer;

    public Collider(CollisionLayer layer) {
        this(layer, 1.0);
    }

    public Collider(CollisionLayer layer, double size) {
        this.layer = layer;
        this.width = size;
        this.height = size;
    }

    public final Vector2d origin = new Vector2d();

    private transient double lastRotation = Double.NaN;
    private transient Vector2d[] vertices = new Vector2d[]{
            new Vector2d(), new Vector2d(), new Vector2d(), new Vector2d()
    };

    private static final RotatedRectangle tmpBounds = new RotatedRectangle();

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
