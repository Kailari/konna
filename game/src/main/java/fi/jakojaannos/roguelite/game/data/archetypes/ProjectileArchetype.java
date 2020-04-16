package fi.jakojaannos.roguelite.game.data.archetypes;

import org.joml.Vector2d;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.data.resources.Entities;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.components.weapon.ProjectileStats;

public class ProjectileArchetype {
    public static EntityHandle createWeaponProjectile(
            final Entities entities,
            final Vector2d position,
            final Vector2d velocity,
            final DamageSource<?> source,
            final CollisionLayer collisionLayer,
            final long timestamp,
            final long duration,
            final double pushForce,
            final double damage
    ) {
        final var entity = entities.createEntity();
        entity.addComponent(new Transform(position));
        entity.addComponent(new Velocity(velocity));
        entity.addComponent(new ProjectileStats(damage, source, pushForce));
        entity.addComponent(new Collider(collisionLayer, 0.3, 1.2, 0.15, 0.15));
        entity.addComponent(new SpriteInfo("sprites/projectile"));
        entity.addComponent(new RotateTowardsVelocityTag());
        if (duration > 0) {
            entity.addComponent(Lifetime.ticks(timestamp, duration));
        }

        return entity;
    }

    public static EntityHandle createShotgunProjectile(
            final Entities entities,
            final Vector2d position,
            final Vector2d velocity,
            final DamageSource<?> source,
            final CollisionLayer collisionLayer,
            final long timestamp,
            final long duration,
            final double pushForce,
            final double damage
    ) {
        final var entity = entities.createEntity();
        entity.addComponent(new Transform(position));
        entity.addComponent(new Velocity(velocity));
        entity.addComponent(new ProjectileStats(damage, source, pushForce));
        entity.addComponent(new Collider(collisionLayer, 0.3, 0.3, 0.15, 0.15));
        entity.addComponent(new SpriteInfo("sprites/pellet"));
        entity.addComponent(new RotateTowardsVelocityTag());
        if (duration > 0) {
            entity.addComponent(Lifetime.ticks(timestamp, duration));
        }

        return entity;
    }
}
