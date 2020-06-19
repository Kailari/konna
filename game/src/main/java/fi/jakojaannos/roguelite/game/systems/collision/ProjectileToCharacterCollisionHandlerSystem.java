package fi.jakojaannos.roguelite.game.systems.collision;

import org.joml.Vector2d;

import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.utilities.Optionals;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.DamageInstance;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.components.weapon.ProjectileStats;
import fi.jakojaannos.roguelite.game.data.resources.collision.Collisions;

public class ProjectileToCharacterCollisionHandlerSystem implements EcsSystem<ProjectileToCharacterCollisionHandlerSystem.Resources, ProjectileToCharacterCollisionHandlerSystem.EntityData, EcsSystem.NoEvents> {
    private final Vector2d temp = new Vector2d();

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var timeManager = resources.timeManager;
        final var collisions = resources.collisions;

        entities.forEach(entity -> {
            final var stats = entity.getData().projectileStats;
            final var velocity = entity.getData().velocity;

            final var entityCollisions = collisions.getEventsFor(entity.getHandle())
                                                   .stream()
                                                   .map(CollisionEvent::collision)
                                                   .filter(Collision::isEntity)
                                                   .map(Collision::getAsEntityCollision);

            for (final var collision : (Iterable<Collision.EntityCollision>) entityCollisions::iterator) {
                final var maybeHealth = collision.getOther().getComponent(Health.class);
                final var maybePhysics = collision.getOther().getComponent(Physics.class);

                if (Optionals.anyPresent(maybeHealth, maybePhysics)) {
                    maybePhysics.ifPresent(physics -> applyKnockback(stats, velocity, physics));
                    maybeHealth.ifPresent(health -> dealDamage(timeManager, stats, health));

                    entity.destroy();
                    break;
                }
            }
        });
    }

    private void dealDamage(
            final TimeManager timeManager,
            final ProjectileStats stats,
            final Health health
    ) {
        health.addDamageInstance(new DamageInstance(stats.damage,
                                                    stats.damageSource),
                                 timeManager.getCurrentGameTime());
    }

    private void applyKnockback(
            final ProjectileStats stats,
            final Velocity velocity,
            final Physics physics
    ) {
        if (velocity.lengthSquared() == 0) {
            return;
        }
        this.temp.set(velocity).normalize(stats.pushForce);
        physics.applyForce(this.temp);
    }

    public static record Resources(
            Collisions collisions,
            TimeManager timeManager
    ) {}

    public static record EntityData(
            ProjectileStats projectileStats,
            Velocity velocity
    ) {}
}
