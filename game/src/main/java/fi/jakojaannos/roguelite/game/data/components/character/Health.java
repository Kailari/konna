package fi.jakojaannos.roguelite.game.data.components.character;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.game.data.DamageInstance;

@NoArgsConstructor
public class Health implements Component {

    public List<DamageInstance> damageInstances = new ArrayList<>();

    public double maxHealth = 100.0;

    /**
     * Use addDamageInstance(...) to modify this!
     */
    public double currentHealth = 100.0;

    public long lastDamageInstanceTimeStamp = -10000L;

    public Health(final double maxHp, final double currentHp) {
        this.maxHealth = maxHp;
        this.currentHealth = currentHp;
    }

    public Health(final double maxHp) {
        this.maxHealth = maxHp;
        this.currentHealth = maxHp;
    }

    public void addDamageInstance(final DamageInstance dmg, final long timeStamp) {
        this.damageInstances.add(dmg);
        if (timeStamp >= this.lastDamageInstanceTimeStamp) this.lastDamageInstanceTimeStamp = timeStamp;
    }

    public double asPercentage() {
        if (this.maxHealth == 0.0) return 0.0;
        return this.currentHealth / this.maxHealth;
    }
}
