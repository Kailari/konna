package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.components.character.JumpingMovementAbility;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.FollowerEnemyAI;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SplitOnDeath;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;
import lombok.val;

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
        return createSlime(entityManager, xPos, yPos, maxHp, size, mass, slimeSize);
    }

    public static Entity createSlime(
            final EntityManager entityManager,
            final double xPos,
            final double yPos,
            final double maxHp,
            final double size,
            final double mass,
            final double slimeSize
    ) {
        val slime = entityManager.createEntity();
        entityManager.addComponentTo(slime, new Transform(xPos, yPos));
        entityManager.addComponentTo(slime, new Health(maxHp));
        entityManager.addComponentTo(slime, createCollider(size, size));
        entityManager.addComponentTo(slime, createPhysics(mass));
        entityManager.addComponentTo(slime, new Velocity());
        entityManager.addComponentTo(slime, new CharacterInput());
        entityManager.addComponentTo(slime, new EnemyTag());
        entityManager.addComponentTo(slime, new CharacterAbilities(new DamageSource.Entity(slime)));
        entityManager.addComponentTo(slime, new EnemyMeleeWeaponStats());
        entityManager.addComponentTo(slime, createSplitOnDeath(slimeSize));
        entityManager.addComponentTo(slime, createMovementAbility(slimeSize));
        entityManager.addComponentTo(slime, createSpriteInfo());
        entityManager.addComponentTo(slime, new FollowerEnemyAI(100, 0));

        return slime;
    }

    private static Physics createPhysics(final double mass) {
        final var physics = new Physics(mass);
        physics.friction = 100.0;
        return physics;
    }

    private static JumpingMovementAbility createMovementAbility(final double slimeSize) {
        final var movementAbility = new JumpingMovementAbility();
        movementAbility.jumpForce = 5.0 * slimeSize;
        return movementAbility;
    }

    private static SplitOnDeath createSplitOnDeath(double slimeSize) {
        val split = new SplitOnDeath();
        split.size = slimeSize;
        return split;
    }

    private static Collider createCollider(double width, double height) {
        return new Collider(CollisionLayer.ENEMY, width, height, width / 2, height / 2);
    }

    private static SpriteInfo createSpriteInfo() {
        val sprite = new SpriteInfo();
        sprite.spriteName = "sprites/slime";

        return sprite;
    }
}
