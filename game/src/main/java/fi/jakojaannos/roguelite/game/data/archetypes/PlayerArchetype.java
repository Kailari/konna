package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.val;

import javax.annotation.Nonnull;

public class PlayerArchetype {

    public static Entity create(
            final EntityManager entityManager,
            final Transform transform
    ) {
        val player = entityManager.createEntity();
        entityManager.addComponentTo(player, transform);
        entityManager.addComponentTo(player, new Velocity());
        entityManager.addComponentTo(player, new CharacterInput());
        entityManager.addComponentTo(player, new CharacterAbilities());
        entityManager.addComponentTo(player, createCollider());
        entityManager.addComponentTo(player, new PlayerTag());
        entityManager.addComponentTo(player, createCharacterStats());
        entityManager.addComponentTo(player, createWeaponStats());
        entityManager.addComponentTo(player, createSpriteInfo());
        entityManager.addComponentTo(player, new Health(10));
        return player;
    }

    @Nonnull
    private static Collider createCollider() {
        val collider = new Collider(CollisionLayer.PLAYER);
        collider.width = 1.0;
        collider.height = 1.0;
        collider.origin.set(0.5);
        return collider;
    }

    private static CharacterStats createCharacterStats() {
        return new CharacterStats(
                10.0f,
                100.0f,
                150.0f
        );
    }

    private static BasicWeaponStats createWeaponStats() {
        return new BasicWeaponStats(
                20.0,
                40.0,
                2.5
        );
    }

    private static SpriteInfo createSpriteInfo() {
        val sprite = new SpriteInfo();
        sprite.spriteName = "sprites/player";

        return sprite;
    }
}
