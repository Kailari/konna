package fi.jakojaannos.roguelite.game.systems.collision;

import lombok.extern.slf4j.Slf4j;
import org.joml.Vector2d;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.DamageInstance;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.components.weapon.ProjectileStats;
import fi.jakojaannos.roguelite.game.data.resources.collision.Collisions;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

@Slf4j
public class ProjectileToCharacterCollisionHandlerSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.COLLISION_HANDLER)
                    .requireResource(Collisions.class)
                    .withComponent(RecentCollisionTag.class)
                    .withComponent(ProjectileStats.class)
                    .withComponent(Velocity.class);
    }

    private final Vector2d temp = new Vector2d();

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var timeManager = world.getResource(Time.class);
        final var entityManager = world.getEntityManager();
        final var collisions = world.getOrCreateResource(Collisions.class);

        entities.forEach(entity -> {
            final var stats = entityManager.getComponentOf(entity, ProjectileStats.class).orElseThrow();
            final var velocity = entityManager.getComponentOf(entity, Velocity.class).orElseThrow();

            final var entityCollisions = collisions.getEventsFor(entity)
                                                   .stream()
                                                   .map(CollisionEvent::getCollision)
                                                   .filter(Collision::isEntity)
                                                   .map(Collision::getAsEntityCollision);

            for (final var collision : (Iterable<Collision.EntityCollision>) entityCollisions::iterator) {
                if (entityManager.hasComponent(collision.getOther(), Health.class)) {
                    final var health = entityManager.getComponentOf(collision.getOther(), Health.class)
                                                    .orElseThrow();
                    health.addDamageInstance(new DamageInstance(stats.damage,
                                                                stats.damageSource),
                                             timeManager.getCurrentGameTime());
                    entityManager.getComponentOf(collision.getOther(), Physics.class).ifPresent(physics -> {
                        this.temp.set(velocity).normalize(stats.pushForce);
                        physics.applyForce(this.temp);
                    });
                    entityManager.destroyEntity(entity);
                    break;
                }
            }
        });
    }
}
