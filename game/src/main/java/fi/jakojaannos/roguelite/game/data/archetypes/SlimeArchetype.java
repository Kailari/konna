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
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SplitOnDeath;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponInventory;
import fi.jakojaannos.roguelite.game.data.resources.Weapons;

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

    public static EntityHandle createLargeSlime(
            final Entities entities,
            final Transform spawnerTransform,
            final SpawnerComponent spawnerComponent
    ) {
        return createLargeSlime(entities, spawnerTransform);
    }

    public static EntityHandle createLargeSlime(
            final Entities entities,
            final Transform transform
    ) {
        return createSlimeOfSize(entities, transform, LARGE_SLIME_SIZE);
    }

    public static EntityHandle createSlimeOfSize(
            final Entities entities,
            final Transform transform,
            final double slimeSize
    ) {
        final var maxHp = Math.floor(getStatValue(slimeSize, HP_CURVE_A, HP_CURVE_B, HP_CURVE_C, HP_MIN, HP_MAX));
        final var spriteSize = getStatValue(slimeSize, SIZE_CURVE_A, SIZE_CURVE_B, SIZE_CURVE_C, SIZE_MIN, SIZE_MAX);
        final var mass = getStatValue(slimeSize, MASS_CURVE_A, MASS_CURVE_B, MASS_CURVE_C, MASS_MIN, MASS_MAX);
        final var jumpForce = (0.0 + 5.0 * slimeSize);
        return createSlime(entities, transform, maxHp, spriteSize, mass, jumpForce, slimeSize);
    }

    public static EntityHandle createSlime(
            final Entities entities,
            final Transform transform,
            final double maxHp,
            final double spriteSize,
            final double mass,
            final double jumpForce,
            final double slimeSize
    ) {
        final var entity = entities.createEntity(new Health(maxHp),
                                                 new SplitOnDeath(slimeSize),
                                                 new Collider(CollisionLayer.ENEMY,
                                                              spriteSize,
                                                              spriteSize,
                                                              spriteSize / 2,
                                                              spriteSize / 2),
                                                 new SpriteInfo("sprites/slime"),
                                                 new EnemyTag(),
                                                 new Transform(transform),
                                                 new Velocity(),
                                                 Physics.builder().friction(10.0).mass(mass).build(),
                                                 new FollowerAI(100, 1),
                                                 new MovementInput(),
                                                 JumpingMovementAbility.builder().jumpForce(jumpForce).build(),
                                                 new AttackAI(spriteSize * 0.6),
                                                 new WeaponInput(),
                                                 new WeaponInventory(Weapons.SLIME_MELEE));
        entity.addComponent(new AttackAbility(new DamageSource.Entity(entity),
                                              CollisionLayer.ENEMY,
                                              0.0,
                                              0.0));

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
