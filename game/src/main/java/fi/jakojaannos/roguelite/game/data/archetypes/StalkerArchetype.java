package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.character.MovementStats;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.StalkerAI;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;
import lombok.val;

public class StalkerArchetype {
    public static Entity spawnStalker(
            EntityManager entityManager,
            Transform spawnerTransform,
            SpawnerComponent spawnerComponent
    ) {
        return create(entityManager, new Transform(spawnerTransform));
    }

    public static Entity create(
            final EntityManager entityManager,
            double x,
            double y
    ) {
        return create(
                entityManager,
                new Transform(x, y)
        );
    }


    public static Entity create(
            final EntityManager entityManager,
            final Transform transform
    ) {
        val stalker = entityManager.createEntity();
        entityManager.addComponentTo(stalker, transform);
        entityManager.addComponentTo(stalker, new Velocity());
        entityManager.addComponentTo(stalker, new CharacterInput());
        entityManager.addComponentTo(stalker, new Health(2));
        entityManager.addComponentTo(stalker, new Collider(CollisionLayer.ENEMY, 1.0, 1.0, 0.5, 0.5));
        entityManager.addComponentTo(stalker, createMovementStats());
        entityManager.addComponentTo(stalker, createStalkerAi());
        entityManager.addComponentTo(stalker, createSpriteInfo());
        entityManager.addComponentTo(stalker, new EnemyTag());
        entityManager.addComponentTo(stalker, new CharacterAbilities(new DamageSource.Entity(stalker)));
        entityManager.addComponentTo(stalker, new EnemyMeleeWeaponStats());

        return stalker;
    }


    private static MovementStats createMovementStats() {
        return new MovementStats(
                1.0,
                100.0,
                800.0
        );
    }

    private static StalkerAI createStalkerAi() {
        return new StalkerAI(250.0f, 50.0f, 8.0f);
    }


    private static SpriteInfo createSpriteInfo() {
        val sprite = new SpriteInfo();
        sprite.spriteName = "sprites/enemy";

        return sprite;
    }
}
