package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.data.resources.Entities;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.TurretTag;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.LookAtTargetTag;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.AttackAI;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponInventory;
import fi.jakojaannos.roguelite.game.data.resources.Weapons;
import fi.jakojaannos.roguelite.game.weapons.InventoryWeapon;

public class TurretArchetype {
    public static EntityHandle create(
            final Entities entities,
            final TimeManager timeManager,
            final Transform transform
    ) {
        final var turret = entities.createEntity();
        turret.addComponent(new Transform(transform));
        turret.addComponent(new SpriteInfo("sprites/turret"));
        turret.addComponent(new Collider(CollisionLayer.NONE, 1.0, 1.0, 0.5, 0.5));
        turret.addComponent(new LookAtTargetTag());
        turret.addComponent(new TurretTag());
        turret.addComponent(new AttackAI(EnemyTag.class, 15.0));
        turret.addComponent(new WeaponInput());
        turret.addComponent(new AttackAbility(DamageSource.Generic.UNDEFINED,
                                              CollisionLayer.PLAYER_PROJECTILE,
                                              0.375,
                                              -1.0));
        final var wepInv = new WeaponInventory(10);
        turret.addComponent(wepInv);

        wepInv.slots[0] = new InventoryWeapon(Weapons.TURRET_GATLING);

        return turret;
    }
}
