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
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SplitOnDeath;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class SlimeArchetype {
    public static final double LARGE_SLIME_SIZE = 16.0;
    public static final double SIZE_SCALE = 11.0;
    public static final int MAX_HP_SIZE_COEFFICIENT = 7;
    public static final double SIZE_COEFFICIENT = 1.15;
    public static final double MASS_SIZE_COEFFICIENT = 23.0;

    public static Entity createLargeSlime(
            final EntityManager entityManager,
            final Transform spawnerTransform,
            final SpawnerComponent spawnerComponent
    ) {
        return createLargeSlime(entityManager, spawnerTransform.position.x, spawnerTransform.position.y);
    }

    public static Entity createLargeSlime(
            final EntityManager entityManager,
            final double xPos,
            final double yPos
    ) {
        return createSlimeOfSize(entityManager, xPos, yPos, LARGE_SLIME_SIZE);
    }

    public static Entity createSlimeOfSize(
            final EntityManager entityManager,
            final double xPos,
            final double yPos,
            final double slimeSize
    ) {
        final var maxHp = (26.0 + MAX_HP_SIZE_COEFFICIENT * slimeSize) / SIZE_SCALE;
        final var size = (5.45 + SIZE_COEFFICIENT * slimeSize) / SIZE_SCALE;
        final var mass = (-1.0 + MASS_SIZE_COEFFICIENT * slimeSize) / SIZE_SCALE;
        final var jumpForce = (0.0 + 5.0 * slimeSize);
        return createSlime(entityManager, xPos, yPos, maxHp, size, mass, jumpForce, slimeSize);
    }

    public static Entity createSlime(
            final EntityManager entityManager,
            final double xPos,
            final double yPos,
            final double maxHp,
            final double size,
            final double mass,
            final double jumpForce,
            final double slimeSize
    ) {
        final var entity = entityManager.createEntity();

        entityManager.addComponentTo(entity, new Health(maxHp));
        entityManager.addComponentTo(entity, new SplitOnDeath(slimeSize));
        entityManager.addComponentTo(entity, new Collider(CollisionLayer.ENEMY, size, size, size / 2, size / 2));
        entityManager.addComponentTo(entity, new SpriteInfo("sprites/slime"));
        entityManager.addComponentTo(entity, new EnemyTag());

        entityManager.addComponentTo(entity, new Transform(xPos, yPos));
        entityManager.addComponentTo(entity, new Velocity());
        entityManager.addComponentTo(entity, Physics.builder().friction(100.0).mass(mass).build());

        entityManager.addComponentTo(entity, new FollowerAI(100, 1));
        entityManager.addComponentTo(entity, new MovementInput());
        entityManager.addComponentTo(entity, JumpingMovementAbility.builder().jumpForce(jumpForce).build());

        entityManager.addComponentTo(entity, new AttackAI(size * 0.6));
        entityManager.addComponentTo(entity, new WeaponInput());
        entityManager.addComponentTo(entity, new AttackAbility(new DamageSource.Entity(entity), CollisionLayer.ENEMY));
        entityManager.addComponentTo(entity, new WeaponStats(1.0, 10.0, 2.0, 0.0, 15));

        return entity;
    }
}
