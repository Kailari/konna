package fi.jakojaannos.roguelite.game.data.archetypes;

import org.joml.Vector2d;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.data.resources.Entities;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.components.weapon.Fuse;
import fi.jakojaannos.roguelite.game.data.components.weapon.GrenadeStats;

public class GrenadeArchetype {
    public static EntityHandle createGrenade(
            final Entities entityManager,
            final Vector2d position,
            final Vector2d velocity,
            final CollisionLayer collisionLayer,
            final GrenadeStats stats,
            final DamageSource<?> damageSource,
            final long timestamp,
            final long airTime
    ) {
        return entityManager.createEntity(
                new Transform(position),
                new Velocity(velocity),
                new Collider(collisionLayer, 1.0, 1.0, 0.5, 0.5),
                stats,
                new Physics.Builder()
                        .friction(90.0)
                        .mass(7.5)
                        .build(),
                new SpriteInfo("sprites/bomb"),
                new InAir(timestamp, airTime),
                new Fuse(timestamp, stats.fuseTime, damageSource)
        );
    }
}
