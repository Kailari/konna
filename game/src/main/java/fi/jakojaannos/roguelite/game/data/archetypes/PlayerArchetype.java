package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.World;
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
    public static EntityHandle create(
            final World world,
            final TimeManager timeManager,
            final Transform transform
    ) {
        final var player = world.createEntity();
        player.addComponent(transform);
        player.addComponent(new Velocity());
        player.addComponent(Physics.builder().friction(42.0 * 2).build());
        player.addComponent(new MovementInput());
        player.addComponent(new WeaponInput());
        player.addComponent(new AttackAbility(new DamageSource.Entity(player),
                                              CollisionLayer.PLAYER_PROJECTILE,
                                              0.25,
                                              -0.5));
        player.addComponent(new Collider(CollisionLayer.PLAYER, 1.0, 1.0, 0.5, 0.5));
        player.addComponent(new PlayerTag());
        player.addComponent(new LookAtTargetTag());
        player.addComponent(new WalkingMovementAbility(10.0f, 69.0f * 1.5));

        final var weaponInventory = new WeaponInventory(10);
        player.addComponent(weaponInventory);
        final var assaultRifleStats = WeaponStats.builder()
                                                 .timeBetweenShots(timeManager.convertToTicks(1.0 / 2.5))
                                                 .projectileSpeed(80.0)
                                                 .spread(2.5)
                                                 .projectileSpeedNoise(4.0)
                                                 .projectileLifetimeInTicks(-1)
                                                 .projectilePushForce(20.0)
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
                                            .projectileSpeed(12.0)
                                            .spread(0.0)
                                            .projectileSpeedNoise(0.0)
                                            .projectileLifetimeInTicks(-1)
                                            .projectilePushForce(0.0)
                                            .magazineCapacity(100)
                                            .reloadTimeInTicks(timeManager.convertToTicks(0.8))
                                            .build();
        weaponInventory.equip(0, new InventoryWeapon<>(SimpleWeapon.createBasicWeapon(), assaultRifleStats));
        weaponInventory.equip(1, new InventoryWeapon<>(SimpleWeapon.createShotgunWeapon(), shotgunStats));
        weaponInventory.equip(2, new InventoryWeapon<>(new GrenadeWeapon(), grenadeStats));
        player.addComponent(new SpriteInfo("sprites/player"));
        player.addComponent(new Health(10));
        return player;
    }
}
