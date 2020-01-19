package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.NoArgsConstructor;
import org.joml.Vector2d;

@NoArgsConstructor
public class Physics implements Component {

    public double mass = 1.0;
    public Vector2d acceleration = new Vector2d(0.0, 0.0);

    public Physics(double mass) {
        this.mass = mass;
    }

    public void applyForce(Vector2d force) {
        if (mass == 0.0) return;
        acceleration.add(force.mul(1.0 / mass));
    }
}
