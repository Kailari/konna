package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.val;

public class StalkerArchetype {

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
        entityManager.addComponentTo(stalker, new Health(3));
        entityManager.addComponentTo(stalker, new Collider(CollisionLayer.ENEMY));
        entityManager.addComponentTo(stalker, createCharacterStats());
        entityManager.addComponentTo(stalker, createStalkerAi());
        entityManager.addComponentTo(stalker, createSpriteInfo());

        return stalker;
    }


    private static CharacterStats createCharacterStats() {
        return new CharacterStats(
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
