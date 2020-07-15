package fi.jakojaannos.roguelite.game.data.components;

import org.joml.Vector2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Physics {
    public final Vector2d acceleration = new Vector2d(0.0, 0.0);
    public double mass;
    public double friction;
    // TODO: add to builder
    public double drag;

    private Physics(final double mass, final double friction) {
        this.mass = mass;
        this.friction = friction;
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

        public Builder mass(final double mass) {
            this.mass = mass;
            return this;
        }

        public Builder friction(final double friction) {
            this.friction = friction;
            return this;
        }

        public Physics build() {
            return new Physics(this.mass, this.friction);
        }
    }
}
