package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.weapon.ProjectileStats;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;
import lombok.val;
import org.joml.Vector2d;

import javax.annotation.Nonnull;

public class BasicProjectileArchetype {
    public static Entity create(
            final DamageSource<?> source,
            final World world,
            final double projectileX,
            final double projectileY,
            final Vector2d direction,
            final double projectileSpeed,
            final Vector2d spreadOffset
    ) {
        return create(world,
                      new Transform(projectileX, projectileY),
                      new Velocity(direction.normalize(projectileSpeed, new Vector2d())
                                            .add(spreadOffset)),
                      source);
    }


    public static Entity create(
            final World world,
            final Transform transform,
            final Velocity velocity,
            final DamageSource<?> source
    ) {
        val entities = world.getEntityManager();

        val projectile = entities.createEntity();
        entities.addComponentTo(projectile, createProjectileStats(source));
        entities.addComponentTo(projectile, createCollider());
        entities.addComponentTo(projectile, transform);
        entities.addComponentTo(projectile, velocity);
        entities.addComponentTo(projectile, createSpriteInfo());

        return projectile;
    }

    @Nonnull
    private static Collider createCollider() {
        val collider = new Collider(CollisionLayer.PLAYER_PROJECTILE);
        collider.width = 0.30;
        collider.height = 0.30;
        collider.origin.set(0.15);
        return collider;
    }

    private static ProjectileStats createProjectileStats(final DamageSource<?> source) {
        return new ProjectileStats(1.0, source);
    }


    private static SpriteInfo createSpriteInfo() {
        val sprite = new SpriteInfo();
        sprite.spriteName = "sprites/projectile";

        return sprite;
    }
}
