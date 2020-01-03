package fi.jakojaannos.roguelite.game.data.components.character;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.game.data.DamageInstance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class Health implements Component {

    public List<DamageInstance> damageInstances = new ArrayList<>();

    public double maxHealth = 100.0;

    /**
     * Use addDamageInstance(...) to modify this!
     */
    public double currentHealth = 100.0;

    public long lastDamageInstanceTimeStamp = -10000L;
    public boolean healthBarAlwaysVisible = false;

    public Health(double maxHp, double currentHp) {
        this.maxHealth = maxHp;
        this.currentHealth = currentHp;
    }

    public Health(double maxHp) {
        this.maxHealth = maxHp;
        this.currentHealth = maxHp;
    }

    public void addDamageInstance(DamageInstance dmg, long timeStamp) {
        damageInstances.add(dmg);
        if (timeStamp >= lastDamageInstanceTimeStamp) lastDamageInstanceTimeStamp = timeStamp;
    }

    public double asPercentage() {
        if (maxHealth == 0.0) return 0.0;
        return currentHealth / maxHealth;
    }
}
