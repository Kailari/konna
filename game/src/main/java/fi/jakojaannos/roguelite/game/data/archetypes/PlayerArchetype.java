package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.*;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;
import fi.jakojaannos.roguelite.game.weapons.GrenadeWeapon;
import fi.jakojaannos.roguelite.game.weapons.InventoryWeapon;
import fi.jakojaannos.roguelite.game.weapons.SimpleWeapon;
import fi.jakojaannos.roguelite.game.weapons.WeaponInventory;

public class PlayerArchetype {
    public static Entity create(
            final EntityManager entityManager,
            final TimeManager timeManager,
            final Transform transform
    ) {
        final var player = entityManager.createEntity();
        entityManager.addComponentTo(player, transform);
        entityManager.addComponentTo(player, new Velocity());
        entityManager.addComponentTo(player, Physics.builder().friction(42.0 * 2).build());
        entityManager.addComponentTo(player, new MovementInput());
        entityManager.addComponentTo(player, new WeaponInput());
        entityManager.addComponentTo(player, new AttackAbility(new DamageSource.Entity(player),
                                                               CollisionLayer.PLAYER_PROJECTILE,
                                                               0.25,
                                                               -0.5));
        entityManager.addComponentTo(player, new Collider(CollisionLayer.PLAYER, 1.0, 1.0, 0.5, 0.5));
        entityManager.addComponentTo(player, new PlayerTag());
        entityManager.addComponentTo(player, new LookAtTargetTag());
        entityManager.addComponentTo(player, new WalkingMovementAbility(10.0f, 69.0f * 1.5));
        final var wepInv = new WeaponInventory(10);
        entityManager.addComponentTo(player, wepInv);
        final var wepStats = WeaponStats.builder()
                                        .timeBetweenShots(timeManager.convertToTicks(1.0 / 2.5))
                                        .projectileSpeed(80.0)
                                        .spread(2.5)
                                        .projectileSpeedNoise(4.0)
                                        .projectileLifetimeInTicks(-1)
                                        .projectilePushForce(10.0)
                                        .magazineCapacity(10)
                                        .reloadTimeInTicks(timeManager.convertToTicks(2.0))
                                        .build();
        final var shotgunStats = WeaponStats.builder()
                                            .timeBetweenShots(timeManager.convertToTicks(1.0 / 1.5))
                                            .projectileSpeed(50.0)
                                            .spread(10.0)
                                            .projectileSpeedNoise(1.0)
                                            .projectileLifetimeInTicks(10)
                                            .projectilePushForce(7.5)
                                            .magazineCapacity(6)
                                            .reloadTimeInTicks(timeManager.convertToTicks(0.8))
                                            .pelletCount(12)
                                            .damage(0.35)
                                            .build();
        final var grenadeStats = WeaponStats.builder()
                                            .timeBetweenShots(timeManager.convertToTicks(3.0))
                                            .projectileSpeed(1.0)
                                            .spread(0.0)
                                            .projectileSpeedNoise(0.0)
                                            .projectileLifetimeInTicks(-1)
                                            .projectilePushForce(0.0)
                                            .magazineCapacity(100)
                                            .reloadTimeInTicks(timeManager.convertToTicks(0.8))
                                            .build();
        wepInv.equip(0, new InventoryWeapon<>(SimpleWeapon.createBasicWeapon(), wepStats));
        wepInv.equip(1, new InventoryWeapon<>(SimpleWeapon.createShotgunWeapon(), shotgunStats));
        wepInv.equip(2, new InventoryWeapon<>(new GrenadeWeapon(), grenadeStats));
        entityManager.addComponentTo(player, new SpriteInfo("sprites/player"));
        entityManager.addComponentTo(player, new Health(10));
        return player;
    }
}
