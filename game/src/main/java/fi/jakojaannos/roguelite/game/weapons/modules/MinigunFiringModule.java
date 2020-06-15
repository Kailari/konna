package fi.jakojaannos.roguelite.game.weapons.modules;

import org.joml.Vector2d;

import java.util.Random;

import fi.jakojaannos.roguelite.engine.utilities.math.CoordinateHelper;
import fi.jakojaannos.roguelite.game.data.archetypes.ProjectileArchetype;
import fi.jakojaannos.roguelite.game.data.events.GunshotEvent;
import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.ReloadEvent;
import fi.jakojaannos.roguelite.game.weapons.events.TriggerPullEvent;
import fi.jakojaannos.roguelite.game.weapons.events.TriggerReleaseEvent;
import fi.jakojaannos.roguelite.game.weapons.events.WeaponFireEvent;

public class MinigunFiringModule implements WeaponModule<MinigunFiringModule.Attributes> {
    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.weaponFire(this::checkIfReadyToFire, Phase.CHECK);
        hooks.weaponFire(this::fire, Phase.TRIGGER);
        hooks.weaponFire(this::afterFire, Phase.POST);
        hooks.triggerPull(this::triggerPull, Phase.POST);
        hooks.triggerRelease(this::triggerRelease, Phase.POST);
        hooks.reload(this::afterReload, Phase.POST);

        hooks.registerStateFactory(State.class, State::new);
    }

    private void afterReload(
            final Weapon weapon,
            final ReloadEvent reloadEvent,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        state.fireRateIncrease = 0;
        state.spreadDecrease = 0;
    }

    public void triggerPull(
            final Weapon weapon,
            final TriggerPullEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        state.fireRateIncrease = 0;
        state.spreadDecrease = 0;
    }

    public void triggerRelease(
            final Weapon weapon,
            final TriggerReleaseEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        state.fireRateIncrease = 0;
        state.spreadDecrease = 0;
    }

    public void checkIfReadyToFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        final var timeSinceLastAttack = info.timeManager().getCurrentGameTime() - state.lastAttackTimestamp;
        if (timeSinceLastAttack < getTimeBetweenShots(state, attributes)) {
            event.cancel();
        }
    }

    public void afterFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        state.lastAttackTimestamp = info.timeManager().getCurrentGameTime();
        state.spreadDecrease += attributes.spreadDecreasePerShot;
        state.fireRateIncrease += attributes.fireRateIncreasePerShot;
    }

    public void fire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        final var entities = info.entities();
        final var timeManager = info.timeManager();
        final var attackAbility = info.attackAbility();
        final var shooterTransform = info.shooterTransform();

        final var weaponOffset = CoordinateHelper.transformCoordinate(0,
                                                                      0,
                                                                      shooterTransform.rotation,
                                                                      attributes.weaponOffset.x,
                                                                      attributes.weaponOffset.y,
                                                                      new Vector2d());

        final var projectilePos = state.tmpProjectilePos.set(shooterTransform.position)
                                                        .add(weaponOffset);
        final var direction = attackAbility.targetPosition.sub(projectilePos, state.tmpDirection);
        if (direction.lengthSquared() == 0) {
            direction.set(1.0, 0.0);
        } else {
            direction.normalize();
        }

        final var spreadAmount = (state.random.nextDouble() * 2.0 - 1.0) * getSpread(state, attributes);
        final var spreadOffset = state.tmpSpreadOffset.set(direction)
                                                      .perpendicular()
                                                      .mul(spreadAmount);

        final var speedNoise = (state.random.nextDouble() * 2.0 - 1.0) * attributes.projectileSpeedNoise;
        final var actualSpeed = attributes.projectileSpeed + speedNoise;

        final var timestamp = timeManager.getCurrentGameTime();
        ProjectileArchetype.createWeaponProjectile(entities,
                                                   projectilePos,
                                                   direction.normalize(actualSpeed)
                                                            .add(spreadOffset),
                                                   attackAbility.damageSource,
                                                   attackAbility.projectileLayer,
                                                   timestamp,
                                                   attributes.projectileLifetimeInTicks,
                                                   attributes.projectilePushForce,
                                                   attributes.damage);
        info.events().fire(new GunshotEvent(attributes.gunshotVariant));
    }

    private long getTimeBetweenShots(final State state, final Attributes attributes) {
        final var time = attributes.baseTimeBetweenShots - state.fireRateIncrease;
        if (time <= attributes.minTimeBetweenShots) {
            return attributes.minTimeBetweenShots;
        }
        return (long) time;
    }

    private double getSpread(final State state, final Attributes attributes) {
        final var spread = attributes.baseSpread - state.spreadDecrease;
        if (spread <= attributes.minSpread) {
            return attributes.minSpread;
        }
        return spread;
    }

    public static class State {
        private final Vector2d tmpSpreadOffset = new Vector2d();
        private final Vector2d tmpProjectilePos = new Vector2d();
        private final Vector2d tmpDirection = new Vector2d();
        private final Random random = new Random(1337);

        private long lastAttackTimestamp = -1000;
        private double fireRateIncrease;
        private double spreadDecrease;
    }

    public static record Attributes(
            Vector2d weaponOffset,
            long baseTimeBetweenShots,
            double fireRateIncreasePerShot,
            long minTimeBetweenShots,
            double projectileSpeed,
            double baseSpread,
            double spreadDecreasePerShot,
            double minSpread,
            double projectileSpeedNoise,
            long projectileLifetimeInTicks,
            double projectilePushForce,
            double damage,
            GunshotEvent.Variant gunshotVariant
    ) {}
}
