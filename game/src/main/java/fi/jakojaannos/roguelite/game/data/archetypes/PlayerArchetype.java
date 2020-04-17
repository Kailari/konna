package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.resources.Weapons;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.*;
import fi.jakojaannos.roguelite.game.weapons.InventoryWeapon;
import fi.jakojaannos.roguelite.game.weapons.WeaponInventory;
import fi.jakojaannos.roguelite.game.weapons.Weapons;

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

        weaponInventory.equip(0, new InventoryWeapon(Weapons.PLAYER_AR));
        weaponInventory.equip(1, new InventoryWeapon(Weapons.PLAYER_SHOTGUN));

        player.addComponent(new SpriteInfo("sprites/player"));
        player.addComponent(new Health(10));
        return player;
    }
}
