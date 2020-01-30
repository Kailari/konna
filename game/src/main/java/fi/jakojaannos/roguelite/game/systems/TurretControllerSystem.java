package fi.jakojaannos.roguelite.game.systems;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.archetypes.BasicProjectileArchetype;
import fi.jakojaannos.roguelite.game.data.components.BasicTurretComponent;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;

public class TurretControllerSystem implements ECSSystem {
    private static final List<Class<? extends Component>> targetRequirements =
            List.of(EnemyTag.class, Transform.class);

    private static void shoot(
            final World world,
            final TimeManager timeManager,
            final Transform origin,
            final Transform target,
            final BasicTurretComponent turretAI
    ) {
        final var velocity = new Velocity();
        velocity.set(target.position)
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
        if (!isTargetValid(entityManager, turretAI.target, myPos, turretAI.targetingRadiusSquared)) {
            turretAI.target = findNewTarget(entityManager, turretAI, myPos).orElse(null);
        }
    }

    private static boolean isTargetValid(
            final EntityManager entityManager,
            @Nullable final Entity target,
            final Transform turretPos,
            final double targetingRadiusSquared
    ) {
        if (target == null) return false;
        if (target.isMarkedForRemoval()) return false;
        if (!entityManager.hasComponent(target, EnemyTag.class)) return false;
        if (!entityManager.hasComponent(target, Transform.class)) return false;

        final var targetPos = entityManager.getComponentOf(target, Transform.class).orElseThrow();
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
                    final var enemyPos = entityManager.getComponentOf(entity, Transform.class).orElseThrow();
                    return (myPos.position.distanceSquared(enemyPos.position) <= turretAI.targetingRadiusSquared);
                })
                .findAny();
    }

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.INPUT)
                    .withComponent(Transform.class)
                    .withComponent(BasicTurretComponent.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var entityManager = world.getEntityManager();
        final var timeManager = world.getOrCreateResource(Time.class);

        entities.forEach(entity -> {
            final var turretAI = entityManager.getComponentOf(entity, BasicTurretComponent.class).orElseThrow();
            final var myPos = entityManager.getComponentOf(entity, Transform.class).orElseThrow();

            if (timeManager.getCurrentGameTime() <
                    turretAI.lastShotTimestamp + turretAI.shootingCoolDownInTicks)
                return;

            findOrUpdateTarget(entityManager, turretAI, myPos);

            if (turretAI.target != null) {
                final var targetPos = entityManager.getComponentOf(turretAI.target, Transform.class).orElseThrow();
                shoot(world, timeManager, myPos, targetPos, turretAI);
            }
        });
    }
}
