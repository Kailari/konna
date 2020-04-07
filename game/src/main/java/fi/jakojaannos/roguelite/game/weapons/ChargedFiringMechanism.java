package fi.jakojaannos.roguelite.game.weapons;

import org.joml.Vector2d;
import org.joml.Vector2i;

import java.util.Random;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.utilities.math.CoordinateHelper;
import fi.jakojaannos.roguelite.game.data.archetypes.BombArchetype;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class ChargedFiringMechanism implements Weapon.FiringMechanism<ChargedFiringState> {
    private final Vector2d tmpSpreadOffset = new Vector2d();
    private final Vector2d tmpProjectilePos = new Vector2d();
    private final Vector2d tmpDirection = new Vector2d();

    private final Random random = new Random(1337);

    private final ChargedFiringState charge;

    public ChargedFiringMechanism(final ChargedFiringState state) {
        this.charge = state;
    }

    @Override
    public ChargedFiringState createState(final WeaponStats stats) {
        return this.charge;
    }

    @Override
    public boolean isReadyToFire(
            final TimeManager timeManager,
            final ChargedFiringState state,
            final WeaponStats stats
    ) {
        final var timeSinceLastAttack = timeManager.getCurrentGameTime() - state.getLastAttackTimestamp();
        return timeSinceLastAttack >= stats.timeBetweenShots;
    }

    @Override
    public void fire(
            final EntityManager entityManager,
            final Entity shooter,
            final TimeManager timeManager,
            final ChargedFiringState state,
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

        final var minChargeTime = 0;
        final var minChargeValue = 2;
        final var maxChargeTime = 120;
        final var maxChargeValue = 55;
        final var airTime = getAirTime(
                this.tempMin.set(minChargeTime, minChargeValue),
                this.tempMax.set(maxChargeTime, maxChargeValue),
                this.charge);

        // TODO: move these to weaponStats or something
        final var fuseTime = 120;
        final var boomDamage = 2.5;
        final var boomPushForce = 30;
        final var shrapnelDamage = 1.5;

        final var timestamp = timeManager.getCurrentGameTime();
        BombArchetype.createGrenade(entityManager,
                                    projectilePos,
                                    direction.normalize(actualSpeed)
                                             .add(spreadOffset),
                                    attackAbility.damageSource,
                                    attackAbility.projectileLayer,
                                    timestamp,
                                    fuseTime,
                                    airTime,
                                    boomPushForce,
                                    boomDamage,
                                    shrapnelDamage);

        state.setLastAttackTimestamp(timestamp);
        state.setHasFired(true);
    }

    private final Vector2i tempMin = new Vector2i();
    private final Vector2i tempMax = new Vector2i();

    private static long getAirTime(
            final Vector2i minVal,
            final Vector2i maxVal,
            final ChargedFiringState state
    ) {
        // if time charged is less than minVal.x, return minVal.y
        // if time charged is more than maxVal.x, return maxVal.y
        // else return LERP between the two
        if (state.getChargeTime() <= minVal.x) return minVal.y;
        if (state.getChargeTime() >= maxVal.x) return maxVal.y;

        final var diff = maxVal.x - minVal.x;
        if (diff == 0) return minVal.y;

        // y = y0 + (x - x0) * (y1 - y0) / (x1 - x0)
        return (long) (minVal.y
                + (state.getChargeTime() - minVal.x)
                * (maxVal.y - minVal.y)
                / ((double) maxVal.x - minVal.x));
    }
}
