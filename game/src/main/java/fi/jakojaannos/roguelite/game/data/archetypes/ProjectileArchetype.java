package fi.jakojaannos.roguelite.game.data.archetypes;

import org.joml.Vector2d;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.components.weapon.ProjectileStats;

public class ProjectileArchetype {
    public static Entity createWeaponProjectile(
            final EntityManager entityManager,
            final Vector2d position,
            final Vector2d velocity,
            final DamageSource<?> source,
            final CollisionLayer collisionLayer,
            final long timestamp,
            final long duration,
            final double pushForce
    ) {
        return createWeaponProjectile(
                entityManager,
                position,
                velocity,
                source,
                collisionLayer,
                timestamp,
                duration,
                pushForce,
                1.0);
    }

    public static Entity createWeaponProjectile(
            final EntityManager entityManager,
            final Vector2d position,
            final Vector2d velocity,
            final DamageSource<?> source,
            final CollisionLayer collisionLayer,
            final long timestamp,
            final long duration,
            final double pushForce,
            final double damage
    ) {
        final var entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, new Transform(position));
        entityManager.addComponentTo(entity, new Velocity(velocity));
        entityManager.addComponentTo(entity, new ProjectileStats(damage, source, pushForce));
        entityManager.addComponentTo(entity, new Collider(collisionLayer, 0.3, 1.2, 0.15, 0.15));
        entityManager.addComponentTo(entity, new SpriteInfo("sprites/projectile"));
        entityManager.addComponentTo(entity, new RotateTowardsVelocityTag());
        if (duration > 0) {
            entityManager.addComponentTo(entity, Lifetime.ticks(timestamp, duration));
        }

        return entity;
    }

    public static Entity createShotgunProjectile(
            final EntityManager entityManager,
            final Vector2d position,
            final Vector2d velocity,
            final DamageSource<?> source,
            final CollisionLayer collisionLayer,
            final long timestamp,
            final long duration,
            final double pushForce,
            final double damage
    ) {
        final var entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, new Transform(position));
        entityManager.addComponentTo(entity, new Velocity(velocity));
        entityManager.addComponentTo(entity, new ProjectileStats(damage, source, pushForce));
        entityManager.addComponentTo(entity, new Collider(collisionLayer, 0.3, 0.3, 0.15, 0.15));
        entityManager.addComponentTo(entity, new SpriteInfo("sprites/pellet"));
        entityManager.addComponentTo(entity, new RotateTowardsVelocityTag());
        if (duration > 0) {
            entityManager.addComponentTo(entity, Lifetime.ticks(timestamp, duration));
        }

        return entity;
    }
}
