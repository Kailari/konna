package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class Health implements Component {

    public double maxHealth = 100.0, currentHealth = 100.0;

    public Health(double maxHp) {
        this.maxHealth = maxHp;
        this.currentHealth = maxHp;
    }

}
