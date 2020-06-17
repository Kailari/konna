package fi.jakojaannos.roguelite.game.data.components.character;

import org.joml.Vector2d;

import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;

public class AttackAbility {
    public final DamageSource<?> damageSource;
    public final CollisionLayer projectileLayer;
    public final Vector2d weaponOffset = new Vector2d(0.0, 0.0);
    public Vector2d targetPosition = new Vector2d();

    public int equippedSlot;
    public int previousEquippedSlot;

    public AttackAbility(
            final DamageSource<?> damageSource,
            final CollisionLayer projectileLayer,
            final double weaponOffsetX,
            final double weaponOffsetY,
            final int equippedSlot
    ) {
        this.damageSource = damageSource;
        this.projectileLayer = projectileLayer;
        this.weaponOffset.set(weaponOffsetX, weaponOffsetY);

        this.equippedSlot = equippedSlot;
        this.previousEquippedSlot = equippedSlot;
    }
}
