package fi.jakojaannos.roguelite.game.systems.collision;

import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.DamageInstance;
import fi.jakojaannos.roguelite.game.data.components.EnemyMeleeWeaponStats;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.resources.collision.Collisions;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

@Slf4j
public class EnemyToPlayerCollisionHandlerSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.COLLISION_HANDLER)
                    .requireResource(Collisions.class)
                    .withComponent(RecentCollisionTag.class)
                    .withComponent(PlayerTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var timeManager = world.getOrCreateResource(Time.class);
        final var entityManager = world.getEntityManager();
        final var collisions = world.getOrCreateResource(Collisions.class);

        entities.forEach(entity -> {
            final var health = entityManager.getComponentOf(entity, Health.class).orElseThrow();

            final var entityCollisions = collisions.getEventsFor(entity)
                                                   .stream()
                                                   .map(CollisionEvent::getCollision)
                                                   .filter(Collision::isEntity)
                                                   .map(Collision::getAsEntityCollision);

            for (final var collision : (Iterable<Collision.EntityCollision>) entityCollisions::iterator) {
                final var other = collision.getOther();
                if (entityManager.hasComponent(other, EnemyTag.class)
                        && entityManager.hasComponent(other, EnemyMeleeWeaponStats.class)
                        && entityManager.hasComponent(other, CharacterAbilities.class)
                ) {
                    final var abilities = entityManager.getComponentOf(other, CharacterAbilities.class).orElseThrow();
                    final var stats = entityManager.getComponentOf(other, EnemyMeleeWeaponStats.class).orElseThrow();

                    if (abilities.attackTimer < stats.attackRate) {
                        continue;
                    }

                    health.addDamageInstance(new DamageInstance(stats.damage,
                                                                abilities.damageSource),
                                             timeManager.getCurrentGameTime());
                    abilities.attackTimer = 0.0;
                }
            }
        });
    }
}
