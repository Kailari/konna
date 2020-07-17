package fi.jakojaannos.roguelite.game.data.components;

import org.joml.Vector2d;

public class Physics {
    public final Vector2d acceleration = new Vector2d(0.0, 0.0);
    public double mass;
    public double friction;
    public double drag;

    private Physics(final double mass, final double friction, final double drag) {
        this.mass = mass;
        this.friction = friction;
        this.drag = drag;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void applyForce(final Vector2d force) {
        if (this.mass == 0.0) {
            return;
        }
        this.acceleration.add(force.mul(1.0 / this.mass));
    }

    public static class Builder {
        private double mass = 1.0;
        private double friction = 2.0;
        private double drag = 0.0;

        public Builder mass(final double mass) {
            this.mass = mass;
            return this;
        }

        public Builder friction(final double friction) {
            this.friction = friction;
            return this;
        }

        public Builder drag(final double drag) {
            this.drag = drag;
            return this;
        }

        public Physics build() {
            return new Physics(this.mass, this.friction, this.drag);
        }
    }
}
