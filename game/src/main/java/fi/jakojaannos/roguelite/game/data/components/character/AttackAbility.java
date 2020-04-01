package fi.jakojaannos.roguelite.game.data.components.character;

import org.joml.Vector2d;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.weapons.SimpleWeapon;
import fi.jakojaannos.roguelite.game.weapons.Weapon;

public class AttackAbility implements Component {
    public final DamageSource<?> damageSource;
    public final CollisionLayer projectileLayer;
    public final Vector2d weaponOffset = new Vector2d(0.0, 0.0);

    public Weapon equippedWeapon = new SimpleWeapon();

    public Vector2d targetPosition = new Vector2d();
    public long lastAttackTimestamp = -10000;

    @Deprecated
    public AttackAbility(final DamageSource<?> damageSource) {
        this(damageSource, CollisionLayer.PLAYER_PROJECTILE);
    }

    @Deprecated
    public AttackAbility(final DamageSource<?> damageSource, final CollisionLayer projectileLayer) {
        this(damageSource, projectileLayer, 0.0, 0.0);
    }

    @Deprecated
    public AttackAbility(
            final DamageSource<?> damageSource,
            final CollisionLayer projectileLayer,
            final double weaponOffsetX,
            final double weaponOffsetY
    ) {
        this.damageSource = damageSource;
        this.projectileLayer = projectileLayer;
        this.weaponOffset.set(weaponOffsetX, weaponOffsetY);
    }

    public AttackAbility(
            final DamageSource<?> damageSource,
            final CollisionLayer projectileLayer,
            final double weaponOffsetX,
            final double weaponOffsetY,
            final EntityManager entityManager,
            final Entity entity
    ) {
        this.damageSource = damageSource;
        this.projectileLayer = projectileLayer;
        this.weaponOffset.set(weaponOffsetX, weaponOffsetY);
        this.equippedWeapon.equip(entityManager, entity);
    }

}
