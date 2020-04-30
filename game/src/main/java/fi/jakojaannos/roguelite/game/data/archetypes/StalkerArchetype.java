package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.data.resources.Entities;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.components.character.*;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.AttackAI;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.StalkerAI;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponInventory;
import fi.jakojaannos.roguelite.game.data.resources.Weapons;
import fi.jakojaannos.roguelite.game.weapons.InventoryWeapon;

public class StalkerArchetype {
    public static EntityHandle spawnStalker(
            final Entities entities,
            final Transform spawnerTransform,
            final SpawnerComponent spawnerComponent
    ) {
        return create(entities, spawnerTransform);
    }

    public static EntityHandle create(final Entities entities, final Transform transform) {
        final var entity = entities.createEntity(new Transform(transform),
                                                 new Velocity(),
                                                 Physics.builder().friction(200.0).build(),
                                                 new Health(2),
                                                 new Collider(CollisionLayer.ENEMY, 1.0, 1.0, 0.5, 0.5),
                                                 new SpriteInfo("sprites/sheep_red"),
                                                 new EnemyTag(),
                                                 new StalkerAI(),
                                                 new MovementInput(),
                                                 new WalkingMovementAbility(1.0, 250.0),
                                                 new AttackAI(1.25),
                                                 new WeaponInput(),
                                                 new WeaponInventory(Weapons.FOLLOWER_MELEE));
        entity.addComponent(new AttackAbility(new DamageSource.Entity(entity),
                                              CollisionLayer.ENEMY,
                                              0.0,
                                              0.0));

        return entity;
    }
}
