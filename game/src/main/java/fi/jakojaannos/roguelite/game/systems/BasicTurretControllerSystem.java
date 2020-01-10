package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
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

    private static final List<Class<? extends Component>> targetRequirements =
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

            findOrUpdateTarget(entityManager, turretAI, myPos);

            if (turretAI.target != null) {
                val targetPos = entityManager.getComponentOf(turretAI.target, Transform.class).orElseThrow();
                shoot(world, timeManager, myPos, targetPos, turretAI);
            }
        });
    }

    private static void shoot(
            final World world,
            final TimeManager timeManager,
            final Transform origin,
            final Transform target,
            final BasicTurretComponent turretAI
    ) {
        val velocity = new Velocity();
        velocity.velocity
                .set(target.position)
                .sub(origin.position)
                .normalize(turretAI.projectileSpeed);

        BasicProjectileArchetype.create(world, new Transform(origin), velocity, DamageSource.Generic.UNDEFINED);
        turretAI.lastShotTimestamp = timeManager.getCurrentGameTime();
    }

    private static void findOrUpdateTarget(
            final EntityManager entityManager,
            final BasicTurretComponent turretAI,
            final Transform myPos
    ) {
        if (turretAI.target == null || !isTargetValid(entityManager, turretAI.target, myPos, turretAI.targetingRadiusSquared)) {
            turretAI.target = findNewTarget(entityManager, turretAI, myPos).orElse(null);
        }
    }

    private static boolean isTargetValid(
            final EntityManager entityManager,
            final Entity target,
            final Transform turretPos,
            final double targetingRadiusSquared
    ) {
        if (target.isMarkedForRemoval()) return false;
        if (!entityManager.hasComponent(target, EnemyTag.class)) return false;
        if (!entityManager.hasComponent(target, Transform.class)) return false;

        val targetPos = entityManager.getComponentOf(target, Transform.class).orElseThrow();
        return turretPos.position.distanceSquared(targetPos.position) <= targetingRadiusSquared;
    }

    private static Optional<Entity> findNewTarget(
            final EntityManager entityManager,
            final BasicTurretComponent turretAI,
            final Transform myPos
    ) {
        return entityManager
                .getEntitiesWith(targetRequirements)
                .filter(entity -> {
                    val enemyPos = entityManager.getComponentOf(entity, Transform.class).orElseThrow();
                    return (myPos.position.distanceSquared(enemyPos.position) <= turretAI.targetingRadiusSquared);
                })
                .findAny();
    }
}
