package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.archetypes.BasicProjectileArchetype;
import fi.jakojaannos.roguelite.game.data.components.BasicTurretComponent;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import lombok.val;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class BasicTurretControllerSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.INPUT)
                    .withComponent(Transform.class)
                    .withComponent(BasicTurretComponent.class);
    }

    private final List<Class<? extends Component>> targetRequirements =
            List.of(EnemyTag.class, Transform.class);

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val entityManager = world.getEntityManager();
        val timeManager = world.getOrCreateResource(Time.class);

        entities.forEach(entity -> {
            val turretAI = entityManager.getComponentOf(entity, BasicTurretComponent.class).orElseThrow();
            val myPos = entityManager.getComponentOf(entity, Transform.class).orElseThrow();

            if (timeManager.getCurrentGameTime() <
                    turretAI.lastShotTimestamp + turretAI.shootingCoolDownInTicks)
                return;


            boolean hasTarget = false;
            if (turretAI.target == null || !isValidTarget(entityManager, turretAI.target)) {
                val newTarget = findNewTarget(entityManager, turretAI, myPos);
                if (newTarget.isPresent()) {
                    turretAI.target = newTarget.get();
                    hasTarget = true;
                }
            } else {
                hasTarget = true;
            }

            if (hasTarget) {
                val targetPos = entityManager.getComponentOf(turretAI.target, Transform.class).orElseThrow();

                Velocity velocity = new Velocity();
                velocity.velocity
                        .set(targetPos.position)
                        .sub(myPos.position)
                        .normalize(turretAI.projectileSpeed);
                BasicProjectileArchetype.create(world, new Transform(myPos), velocity, DamageSource.Generic.UNDEFINED);

                turretAI.lastShotTimestamp = timeManager.getCurrentGameTime();
            }

        });


    }

    private boolean isValidTarget(EntityManager entityManager, Entity entity) {
        if (entity.isMarkedForRemoval()) return false;

        return (entityManager.hasComponent(entity, EnemyTag.class)
                && entityManager.hasComponent(entity, Transform.class));
    }

    private Optional<Entity> findNewTarget(
            EntityManager entityManager,
            BasicTurretComponent turretAI,
            Transform myPos
    ) {
        return entityManager.getEntitiesWith(targetRequirements).filter(entity -> {
            val enemyPos = entityManager.getComponentOf(entity, Transform.class).orElseThrow();
            return (myPos.position.distanceSquared(enemyPos.position) <= turretAI.targetingRadiusSquared);
        }).findAny();
    }
}
