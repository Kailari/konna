package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.ecs.EntityHandle;
import fi.jakojaannos.riista.ecs.resources.Entities;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.components.character.*;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.AttackAI;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.FollowerAI;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponInventory;
import fi.jakojaannos.roguelite.game.data.resources.Weapons;

public class FollowerArchetype {
    public static EntityHandle spawnFollower(
            final Entities entities,
            final Transform spawnerTransform,
            final SpawnerComponent spawnerComponent
    ) {
        return create(entities, spawnerTransform);
    }

    public static EntityHandle create(
            final Entities entities,
            final Transform transform
    ) {
        final var entity = entities.createEntity(new Transform(transform),
                                                 new Velocity(),
                                                 Physics.builder().friction(35.0).build(),
                                                 new Health(2),
                                                 new Collider(CollisionLayer.ENEMY, 1.0, 1.0, 0.5, 0.5),
                                                 new SpriteInfo("sprites/enemy"),
                                                 new EnemyTag(),
                                                 new FollowerAI(25.0f, 1.0f),
                                                 new MovementInput(),
                                                 new WalkingMovementAbility(4.0, 50.0),
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
