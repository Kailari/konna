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

public class ChargedFiringMechanism implements Weapon.FiringMechanism<HoldToChargeTriggerState> {
    private final Vector2d tmpSpreadOffset = new Vector2d();
    private final Vector2d tmpProjectilePos = new Vector2d();
    private final Vector2d tmpDirection = new Vector2d();

    private final Random random = new Random(1337);

    private final HoldToChargeTriggerState charge;

    public ChargedFiringMechanism(final HoldToChargeTriggerState state) {
        this.charge = state;
    }

    @Override
    public HoldToChargeTriggerState createState(final WeaponStats stats) {
        return this.charge;
    }

    @Override
    public boolean isReadyToFire(
            final TimeManager timeManager,
            final HoldToChargeTriggerState state,
            final WeaponStats stats
    ) {
        final var timeSinceLastAttack = timeManager.getCurrentGameTime() - state.lastAttackTimestamp;
        return timeSinceLastAttack >= stats.timeBetweenShots;
    }

    @Override
    public void fire(
            final EntityManager entityManager,
            final Entity shooter,
            final TimeManager timeManager,
            final HoldToChargeTriggerState state,
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
                                   getProjectileLifetime(state, stats),
                                   stats.projectilePushForce);

        state.lastAttackTimestamp = timestamp;
        state.hasFired = true;
    }

    // TODO: this is more of an proof of concept version of a system that takes in some variable
    //  (such as time charged the shot) and changes firing behaviour based on that
    private final Vector2d tempMinVal = new Vector2d(0, 10);
    private final Vector2d tempMaxVal = new Vector2d(120, 500);
    private final Vector2d temp = new Vector2d();

    private long getProjectileLifetime(
            final HoldToChargeTriggerState state,
            final WeaponStats stats
    ) {
        final var timeCharged = state.chargeEndTimestamp - state.chargeStartTimestamp;
        this.tempMinVal.lerp(this.tempMaxVal, timeCharged / this.tempMaxVal.x, this.temp);
        return (long) this.temp.y;
    }
}
