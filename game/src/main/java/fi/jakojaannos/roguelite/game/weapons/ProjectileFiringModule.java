package fi.jakojaannos.roguelite.game.weapons;

import org.joml.Vector2d;

import java.util.Random;

import fi.jakojaannos.roguelite.engine.utilities.math.CoordinateHelper;
import fi.jakojaannos.roguelite.game.data.archetypes.ProjectileArchetype;

public class ProjectileFiringModule implements WeaponModule<ProjectileFiringState, ProjectileFiringAttributes> {
    private final Vector2d tmpSpreadOffset = new Vector2d();
    private final Vector2d tmpProjectilePos = new Vector2d();
    private final Vector2d tmpDirection = new Vector2d();

    private final Random random = new Random(1337);

    @Override
    public ProjectileFiringState getState(final InventoryWeapon weapon) {
        return weapon.getState().getOrCreateState(ProjectileFiringModule.class, ProjectileFiringState::new);
    }

    @Override
    public ProjectileFiringAttributes getAttributes(final InventoryWeapon weapon) {
        return weapon.getAttributes()
                     .getOrCreateAttributes(ProjectileFiringModule.class, ProjectileFiringAttributes::new);
    }

    @Override
    public void register(final WeaponHooks hooks) {
        hooks.onWeaponFire(this, this::checkIfReadyToFire, Phase.CHECK);
        hooks.onWeaponFire(this, this::fire, Phase.TRIGGER);
        hooks.onWeaponFire(this, this::afterFire, Phase.POST);
    }

    public void checkIfReadyToFire(
            final ProjectileFiringState state,
            final ProjectileFiringAttributes attributes,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var timeSinceLastAttack = info.timeManager().getCurrentGameTime() - state.lastAttackTimestamp;
        if (timeSinceLastAttack < attributes.timeBetweenShots) {
            event.cancel();
        }
    }

    public void afterFire(
            final ProjectileFiringState state,
            final ProjectileFiringAttributes attributes,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        state.lastAttackTimestamp = info.timeManager().getCurrentGameTime();
    }

    public void fire(
            final ProjectileFiringState state,
            final ProjectileFiringAttributes attributes,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var entityManager = info.entityManager();
        final var timeManager = info.timeManager();
        final var attackAbility = info.attackAbility();
        final var shooterTransform = info.shooterTransform();

        final var weaponOffset = CoordinateHelper.transformCoordinate(0,
                                                                      0,
                                                                      shooterTransform.rotation,
                                                                      attributes.weaponOffset.x,
                                                                      attributes.weaponOffset.y,
                                                                      new Vector2d());

        final var projectilePos = this.tmpProjectilePos.set(shooterTransform.position)
                                                       .add(weaponOffset);
        final var direction = attackAbility.targetPosition.sub(projectilePos, this.tmpDirection);
        if (direction.lengthSquared() == 0) {
            direction.set(1.0, 0.0);
        } else {
            direction.normalize();
        }

        final var spreadAmount = (this.random.nextDouble() * 2.0 - 1.0) * attributes.spread;
        final var spreadOffset = this.tmpSpreadOffset.set(direction)
                                                     .perpendicular()
                                                     .mul(spreadAmount);

        final var speedNoise = (this.random.nextDouble() * 2.0 - 1.0) * attributes.projectileSpeedNoise;
        final var actualSpeed = attributes.projectileSpeed + speedNoise;

        final var timestamp = timeManager.getCurrentGameTime();
        ProjectileArchetype.createWeaponProjectile(entityManager,
                                                   projectilePos,
                                                   direction.normalize(actualSpeed)
                                                            .add(spreadOffset),
                                                   attackAbility.damageSource,
                                                   attackAbility.projectileLayer,
                                                   timestamp,
                                                   attributes.projectileLifetimeInTicks,
                                                   attributes.projectilePushForce,
                                                   attributes.damage);
    }
}
