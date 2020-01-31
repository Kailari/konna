package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.*;
import fi.jakojaannos.roguelite.game.data.components.weapon.BasicWeaponStats;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;
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
        entityManager.addComponentTo(player, createPhysics());
        entityManager.addComponentTo(player, new CharacterInput());
        entityManager.addComponentTo(player, new CharacterAbilities(new DamageSource.Entity(player)));
        entityManager.addComponentTo(player, createCollider());
        entityManager.addComponentTo(player, new PlayerTag());
        entityManager.addComponentTo(player, createMovementStats());
        entityManager.addComponentTo(player, createWeaponStats());
        entityManager.addComponentTo(player, createSpriteInfo());
        Health health = new Health(10);
        health.healthBarAlwaysVisible = true;
        entityManager.addComponentTo(player, health);
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

    private static WalkingMovementAbility createMovementStats() {
        return new WalkingMovementAbility(
                10.0f,
                69.0f
        );
    }

    private static Physics createPhysics() {
        final var physics = new Physics();
        physics.friction = 42.0f;
        return physics;
    }

    private static BasicWeaponStats createWeaponStats() {
        return new BasicWeaponStats(
                7.5,
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
