package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.components.character.MovementStats;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SlimeAI;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;
import lombok.val;

public class SlimeArchetype {
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
        return createSlimeOfSize(entityManager, xPos, yPos, 16.0);
    }

    public static Entity createSlimeOfSize(
            final EntityManager entityManager,
            final double xPos,
            final double yPos,
            final double slimeSize
    ) {
        return createSlime(
                entityManager,
                xPos,
                yPos,
                (26.0 + 7 * slimeSize) / 11.0,
                (5.45 + 1.15 * slimeSize) / 11.0,
                (-1.0 + 23.0 * slimeSize) / 11.0,
                slimeSize
        );
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
        entityManager.addComponentTo(slime, new Physics(mass));
        entityManager.addComponentTo(slime, new Velocity());
        entityManager.addComponentTo(slime, new CharacterInput());
        entityManager.addComponentTo(slime, new EnemyTag());
        entityManager.addComponentTo(slime, new CharacterAbilities(new DamageSource.Entity(slime)));
        entityManager.addComponentTo(slime, new EnemyMeleeWeaponStats());
        entityManager.addComponentTo(slime, createSlimeAI(slimeSize));
        entityManager.addComponentTo(slime, createMovementStats());
        entityManager.addComponentTo(slime, createSpriteInfo());

        return slime;
    }

    private static MovementStats createMovementStats() {
        return new MovementStats(
                0,
                2.0,
                100.0
        );
    }

    private static SlimeAI createSlimeAI(double slimeSize) {
        val ai = new SlimeAI();
        ai.slimeSize = slimeSize;
        ai.jumpForce = 5.0 * slimeSize;

        return ai;
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
