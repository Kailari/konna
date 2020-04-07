package fi.jakojaannos.roguelite.game.data.archetypes;

import org.joml.Vector2d;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.*;

public class BombArchetype {
    public static Entity createGrenade(
            final EntityManager entityManager,
            final Vector2d position,
            final Vector2d velocity,
            final DamageSource<?> source,
            final CollisionLayer collisionLayer,
            final long timestamp,
            final long fuseTime,
            final long airTime,
            final double explosionPushForce,
            final double explosionDamage,
            final double shrapnelDamage
    ) {
        final var entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, new Transform(position));
        entityManager.addComponentTo(entity, new Velocity(velocity));
        entityManager.addComponentTo(entity, new Collider(collisionLayer, 1.0, 1.0, 0.5, 0.5));
        entityManager.addComponentTo(entity, new Physics.Builder()
                .friction(45.0)
                .mass(7.5)
                .build());
        entityManager.addComponentTo(entity, new SpriteInfo("sprites/bomb"));
        entityManager.addComponentTo(entity, new InAir(timestamp, airTime));

        return entity;
    }
}
