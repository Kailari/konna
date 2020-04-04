package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.components.character.*;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.AttackAI;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.FollowerAI;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;
import fi.jakojaannos.roguelite.game.weapons.InventoryWeapon;
import fi.jakojaannos.roguelite.game.weapons.SimpleWeapon;
import fi.jakojaannos.roguelite.game.weapons.WeaponInventory;

public class FollowerArchetype {
    public static Entity spawnFollower(
            final EntityManager entityManager,
            final Transform spawnerTransform,
            final SpawnerComponent spawnerComponent
    ) {
        return create(entityManager, new Transform(spawnerTransform));
    }

    public static Entity create(
            final EntityManager entityManager,
            final Transform transform
    ) {
        final var entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, transform);
        entityManager.addComponentTo(entity, new Velocity());
        entityManager.addComponentTo(entity, Physics.builder().friction(35.0).build());
        entityManager.addComponentTo(entity, new Health(2));
        entityManager.addComponentTo(entity, new Collider(CollisionLayer.ENEMY, 1.0, 1.0, 0.5, 0.5));
        entityManager.addComponentTo(entity, new SpriteInfo("sprites/enemy"));
        entityManager.addComponentTo(entity, new EnemyTag());

        entityManager.addComponentTo(entity, new FollowerAI(25.0f, 1.0f));
        entityManager.addComponentTo(entity, new MovementInput());
        entityManager.addComponentTo(entity, new WalkingMovementAbility(4.0, 50.0));

        entityManager.addComponentTo(entity, new AttackAI(1.25));
        entityManager.addComponentTo(entity, new WeaponInput());
        entityManager.addComponentTo(entity, new AttackAbility(new DamageSource.Entity(entity),
                                                               CollisionLayer.ENEMY,
                                                               0.0,
                                                               0.0));
        final var wepInv = new WeaponInventory(1);
        entityManager.addComponentTo(entity, wepInv);
        final var wepStats = WeaponStats.builder()
                                        .timeBetweenShots(20)
                                        .projectileSpeed(10.0)
                                        .spread(2.0)
                                        .projectileSpeedNoise(0.0)
                                        .projectileLifetimeInTicks(10)
                                        .projectilePushForce(0.0)
                                        .build();
        wepInv.equip(0, new InventoryWeapon<>(new SimpleWeapon(), wepStats));

        return entity;
    }
}
