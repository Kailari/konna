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
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponInventory;
import fi.jakojaannos.roguelite.game.data.resources.Weapons;
import fi.jakojaannos.roguelite.game.weapons.InventoryWeapon;

public class PlayerArchetype {
    public static EntityHandle create(
            final World world,
            final TimeManager timeManager,
            final Transform transform
    ) {
        final var weaponInventory = new WeaponInventory(10);
        weaponInventory.slots[1] = new InventoryWeapon(Weapons.PLAYER_AR);
        weaponInventory.slots[2] = new InventoryWeapon(Weapons.PLAYER_SHOTGUN);
        weaponInventory.slots[3] = new InventoryWeapon(Weapons.PLAYER_MINIGUN_OVERHEAT_FROM_SHOTS);
        weaponInventory.slots[4] = new InventoryWeapon(Weapons.PLAYER_MINIGUN_OVERHEAT_OVER_TIME);
        weaponInventory.slots[7] = new InventoryWeapon(Weapons.PLAYER_TURRET_BUILDER);
        weaponInventory.slots[0] = new InventoryWeapon(Weapons.PLAYER_TEST_AR);

        final var player = world.createEntity(
                new Transform(transform),
                new Velocity(),
                Physics.builder().friction(42.0 * 2).build(),
                new MovementInput(),
                new WeaponInput(),
                new Collider(CollisionLayer.PLAYER, 1.0, 1.0, 0.5, 0.5),
                new PlayerTag(),
                new LookAtTargetTag(),
                new WalkingMovementAbility(10.0f, 69.0f * 1.5),
                weaponInventory,
                new SpriteInfo("sprites/player"),
                new Health(10));

        final var attackAbility = new AttackAbility(new DamageSource.Entity(player),
                                                    CollisionLayer.PLAYER_PROJECTILE,
                                                    0.25,
                                                    -0.5);
        attackAbility.equippedSlot = 1;
        attackAbility.previousEquippedSlot = 1;
        player.addComponent(attackAbility);

        return player;
    }
}
