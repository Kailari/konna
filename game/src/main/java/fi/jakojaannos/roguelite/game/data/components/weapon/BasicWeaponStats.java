package fi.jakojaannos.roguelite.game.data.components.weapon;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BasicWeaponStats implements Component {
    public double attackRate;
    public double attackProjectileSpeed;
    public double attackSpread;

    public double attackProjectileSpeedNoise;

    public BasicWeaponStats() {
        this(2.0, 40.0, 2.5);
    }

    public BasicWeaponStats(double attackRate, double attackProjectileSpeed, double attackSpread) {
        this(attackRate, attackProjectileSpeed, attackSpread, 5.0);
    }
}
