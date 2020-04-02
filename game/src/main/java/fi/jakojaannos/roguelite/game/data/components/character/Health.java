package fi.jakojaannos.roguelite.game.data.components.character;

import java.util.ArrayList;
import java.util.List;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.game.data.DamageInstance;

public class Health implements Component {
    public List<DamageInstance> damageInstances = new ArrayList<>();
    public long lastDamageInstanceTimeStamp = -10000L;

    /**
     * NOTE: Use of <code>addDamageInstance(...)</code> should be preferred when modifying HP or
     * dealing damage!
     */
    public double currentHealth;
    public double maxHealth;

    public Health() {
        this(100.0);
    }

    public Health(final double maxHp) {
        this(maxHp, maxHp);
    }

    public Health(final double maxHp, final double currentHp) {
        this.maxHealth = maxHp;
        this.currentHealth = currentHp;
    }

    public void addDamageInstance(final DamageInstance dmg, final long timeStamp) {
        this.damageInstances.add(dmg);
        if (timeStamp >= this.lastDamageInstanceTimeStamp) {
            this.lastDamageInstanceTimeStamp = timeStamp;
        }
    }
}
