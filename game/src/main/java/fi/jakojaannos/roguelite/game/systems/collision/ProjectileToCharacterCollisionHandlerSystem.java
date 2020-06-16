package fi.jakojaannos.roguelite.game.systems.collision;

import org.joml.Vector2d;

import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.World;
import fi.jakojaannos.riista.ecs.legacy.ECSSystem;
import fi.jakojaannos.riista.ecs.legacy.Entity;
import fi.jakojaannos.riista.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.DamageInstance;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.components.weapon.ProjectileStats;
import fi.jakojaannos.roguelite.game.data.resources.collision.Collisions;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

public class ProjectileToCharacterCollisionHandlerSystem implements ECSSystem {
    private final Vector2d temp = new Vector2d();

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.COLLISION_HANDLER)
                    .requireResource(Collisions.class)
                    .withComponent(RecentCollisionTag.class)
                    .withComponent(ProjectileStats.class)
                    .withComponent(Velocity.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var timeManager = world.fetchResource(TimeManager.class);
        final var entityManager = world.getEntityManager();
        final var collisions = world.fetchResource(Collisions.class);

        entities.forEach(entity -> {
            final var stats = entityManager.getComponentOf(entity, ProjectileStats.class)
                                           .orElseThrow();
            final var velocity = entityManager.getComponentOf(entity, Velocity.class).orElseThrow();

            final var entityCollisions = collisions.getEventsFor(entity.asHandle())
                                                   .stream()
                                                   .map(CollisionEvent::collision)
                                                   .filter(Collision::isEntity)
                                                   .map(Collision::getAsEntityCollision);

            for (final var collision : (Iterable<Collision.EntityCollision>) entityCollisions::iterator) {
                final boolean hasHealth = entityManager.hasComponent(collision.getOther().asLegacyEntity(), Health.class);
                final boolean hasPhysics = entityManager.hasComponent(collision.getOther().asLegacyEntity(), Physics.class);
                if (hasHealth || hasPhysics) {
                    entityManager.getComponentOf(collision.getOther().asLegacyEntity(), Physics.class)
                                 .ifPresent(physics -> applyKnockback(stats, velocity, physics));

                    entityManager.getComponentOf(collision.getOther().asLegacyEntity(), Health.class)
                                 .ifPresent(health -> dealDamage(timeManager, stats, health));

                    entityManager.destroyEntity(entity);
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
}
