package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.components.character.*;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.AttackAI;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.FollowerAI;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SplitOnDeath;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;
import fi.jakojaannos.roguelite.game.weapons.InventoryWeapon;
import fi.jakojaannos.roguelite.game.weapons.SimpleWeapon;
import fi.jakojaannos.roguelite.game.weapons.WeaponInventory;

public class SlimeArchetype {
    public static final double LARGE_SLIME_SIZE = 16.0;

    /*
     * Numbers are from our spreadsheet for slime stats (or equivalent to spreadsheet)
     */
    public static final double HP_CURVE_A = 1.31;
    public static final double HP_CURVE_B = -6.15;
    public static final double HP_CURVE_C = 4.77;
    public static final double SIZE_CURVE_A = 0.37;
    public static final double SIZE_CURVE_B = -0.06;
    public static final double SIZE_CURVE_C = 0.26;
    public static final double MASS_CURVE_A = 7.46;
    public static final double MASS_CURVE_B = -1.23;
    public static final double MASS_CURVE_C = -4.85;
    public static final double HP_MIN = 1.0;
    public static final double SIZE_MIN = 0.6;
    public static final double MASS_MIN = 2.0;
    public static final double HP_MAX = 100.0;
    public static final double SIZE_MAX = 5.0;
    public static final double MASS_MAX = 70.0;

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
        final var maxHp = Math.floor(getStatValue(slimeSize, HP_CURVE_A, HP_CURVE_B, HP_CURVE_C, HP_MIN, HP_MAX));
        final var spriteSize = getStatValue(slimeSize, SIZE_CURVE_A, SIZE_CURVE_B, SIZE_CURVE_C, SIZE_MIN, SIZE_MAX);
        final var mass = getStatValue(slimeSize, MASS_CURVE_A, MASS_CURVE_B, MASS_CURVE_C, MASS_MIN, MASS_MAX);
        final var jumpForce = (0.0 + 5.0 * slimeSize);
        return createSlime(entityManager, xPos, yPos, maxHp, spriteSize, mass, jumpForce, slimeSize);
    }

    public static Entity createSlime(
            final EntityManager entityManager,
            final double xPos,
            final double yPos,
            final double maxHp,
            final double spriteSize,
            final double mass,
            final double jumpForce,
            final double slimeSize
    ) {
        final var entity = entityManager.createEntity();

        entityManager.addComponentTo(entity, new Health(maxHp));
        entityManager.addComponentTo(entity, new SplitOnDeath(slimeSize));
        entityManager.addComponentTo(entity, new Collider(CollisionLayer.ENEMY,
                                                          spriteSize,
                                                          spriteSize,
                                                          spriteSize / 2,
                                                          spriteSize / 2));
        entityManager.addComponentTo(entity, new SpriteInfo("sprites/slime"));
        entityManager.addComponentTo(entity, new EnemyTag());

        entityManager.addComponentTo(entity, new Transform(xPos, yPos));
        entityManager.addComponentTo(entity, new Velocity());
        entityManager.addComponentTo(entity, Physics.builder().friction(100.0).mass(mass).build());

        entityManager.addComponentTo(entity, new FollowerAI(100, 1));
        entityManager.addComponentTo(entity, new MovementInput());
        entityManager.addComponentTo(entity, JumpingMovementAbility.builder().jumpForce(jumpForce).build());

        entityManager.addComponentTo(entity, new AttackAI(spriteSize * 0.6));
        entityManager.addComponentTo(entity, new WeaponInput());
        entityManager.addComponentTo(entity, new AttackAbility(new DamageSource.LegacyEntity(entity),
                                                               CollisionLayer.ENEMY,
                                                               0.0,
                                                               0.0));
        final var wepInv = new WeaponInventory(10);
        entityManager.addComponentTo(entity, wepInv);
        final var wepStats = WeaponStats.builder()
                                        .timeBetweenShots(20)
                                        .projectileSpeed(10.0)
                                        .spread(2.0)
                                        .projectileSpeedNoise(0.0)
                                        .projectileLifetimeInTicks(15)
                                        .projectilePushForce(0.0)
                                        .build();
        wepInv.equip(0, new InventoryWeapon<>(SimpleWeapon.createBasicWeapon(), wepStats));

        return entity;
    }

    private static double getStatValue(
            final double x,
            final double a,
            final double b,
            final double c,
            final double min,
            final double max
    ) {
        final double result = a * Math.pow(x, 0.5) + b * Math.pow(0.5, x) + c;
        return Math.min(Math.max(result, min), max);
    }

}
