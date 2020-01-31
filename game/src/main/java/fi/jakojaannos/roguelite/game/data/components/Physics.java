package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.NoArgsConstructor;
import org.joml.Vector2d;

@NoArgsConstructor
public class Physics implements Component {
    public double mass = 1.0;
    public double friction = 2.0;
    public Vector2d acceleration = new Vector2d(0.0, 0.0);

    public Physics(double mass) {
        this.mass = mass;
    }

    public void applyForce(Vector2d force) {
        if (this.mass == 0.0) return;
        this.acceleration.add(force.mul(1.0 / this.mass));
    }
}
