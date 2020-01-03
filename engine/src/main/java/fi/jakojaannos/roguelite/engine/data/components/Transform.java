package fi.jakojaannos.roguelite.engine.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.NoArgsConstructor;
import org.joml.Vector2d;

@NoArgsConstructor
public final class Transform implements Component {
    public Vector2d position = new Vector2d();
    public double rotation = 0.0;

    public void set(final Transform source) {
        this.position.set(source.position);
        this.rotation = source.rotation;
    }

    public Transform(final double x, final double y) {
        this.position.set(x, y);
    }

    public Transform(Transform source) {
        set(source);
    }

    /**
     * Deprecated as transform now has an absolute position which should be treated as the entity
     * center-point
     */
    @Deprecated
    public final double getCenterX() {
        return this.position.x;
    }

    /**
     * Deprecated as transform now has an absolute position which should be treated as the entity
     * center-point
     */
    @Deprecated
    public final double getCenterY() {
        return this.position.y;
    }

    /**
     * Deprecated as transform now has an absolute position which should be treated as the entity
     * center-point
     */
    @Deprecated
    public Vector2d getCenter(final Vector2d result) {
        return result.set(getCenterX(), getCenterY());
    }

    /**
     * Deprecated as transform now has an absolute position.
     */
    @Deprecated
    public void setPosition(double x, double y) {
        setPositionAndSize(x, y, 0, 0);
    }

    /**
     * Deprecated as transform now has an absolute position.
     */
    @Deprecated
    public void setPositionAndSize(double x, double y, double w, double h) {
        this.position.x = x;
        this.position.y = y;
    }
}
