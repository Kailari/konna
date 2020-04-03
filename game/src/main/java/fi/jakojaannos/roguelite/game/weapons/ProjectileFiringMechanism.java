package fi.jakojaannos.roguelite.game.weapons;

import org.joml.Vector2d;

import java.util.Random;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.utilities.math.CoordinateHelper;
import fi.jakojaannos.roguelite.game.data.archetypes.ProjectileArchetype;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class ProjectileFiringMechanism implements Weapon.FiringMechanism<ProjectileFiringState> {
    private final Vector2d tmpSpreadOffset = new Vector2d();
    private final Vector2d tmpProjectilePos = new Vector2d();
    private final Vector2d tmpDirection = new Vector2d();

    private final Random random = new Random(1337);

    @Override
    public ProjectileFiringState createState() {
        return new ProjectileFiringState();
    }

    @Override
    public void fire(
            final EntityManager entityManager,
            final Entity shooter,
            final TimeManager timeManager,
            final ProjectileFiringState state,
            final WeaponStats stats,
            final AttackAbility attackAbility
    ) {
        final var shooterTransform = entityManager.getComponentOf(shooter, Transform.class)
                                                  .orElseThrow();
        final var weaponOffset = CoordinateHelper.transformCoordinate(0,
                                                                      0,
                                                                      shooterTransform.rotation,
                                                                      attackAbility.weaponOffset.x,
                                                                      attackAbility.weaponOffset.y,
                                                                      new Vector2d());

        final var projectilePos = this.tmpProjectilePos.set(shooterTransform.position)
                                                       .add(weaponOffset);
        final var direction = attackAbility.targetPosition.sub(projectilePos, this.tmpDirection);
        if (direction.lengthSquared() == 0) {
            direction.set(1.0, 0.0);
        } else {
            direction.normalize();
        }

        final var spreadAmount = (this.random.nextDouble() * 2.0 - 1.0) * stats.spread;
        final var spreadOffset = this.tmpSpreadOffset.set(direction)
                                                     .perpendicular()
                                                     .mul(spreadAmount);

        final var speedNoise = (this.random.nextDouble() * 2.0 - 1.0) * stats.projectileSpeedNoise;
        final var actualSpeed = stats.projectileSpeed + speedNoise;

        final var timestamp = timeManager.getCurrentGameTime();
        ProjectileArchetype.create(entityManager,
                                   projectilePos,
                                   direction.normalize(actualSpeed)
                                            .add(spreadOffset),
                                   attackAbility.damageSource,
                                   attackAbility.projectileLayer,
                                   timestamp,
                                   stats.projectileLifetimeInTicks,
                                   stats.projectilePushForce);

        state.lastAttackTimestamp = timestamp;
    }

    @Override
    public boolean isReadyToFire(
            final TimeManager timeManager,
            final ProjectileFiringState state,
            final WeaponStats stats
    ) {
        final var timeSinceLastAttack = timeManager.getCurrentGameTime() - state.lastAttackTimestamp;
        return timeSinceLastAttack >= stats.timeBetweenShots;
    }
}
