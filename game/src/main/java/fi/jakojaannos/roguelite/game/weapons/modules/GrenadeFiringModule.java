package fi.jakojaannos.roguelite.game.weapons.modules;

import org.joml.Vector2d;

import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.archetypes.GrenadeArchetype;
import fi.jakojaannos.roguelite.game.data.components.weapon.GrenadeStats;
import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.WeaponFireEvent;

/**
 * Module that throws a grenade. Speed and air time depend on charge amount. Requires {@link ThrowableChargeModule}
 */
public class GrenadeFiringModule implements WeaponModule<GrenadeFiringModule.Attributes>, FiringModule {

    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.weaponFire(this::checkIfCanFire, Phase.CHECK);
        hooks.weaponFire(this::fire, Phase.TRIGGER);
        hooks.weaponFire(this::afterFire, Phase.POST);

        hooks.registerStateFactory(State.class, State::new);
    }

    private void checkIfCanFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        if (!isReadyToFire(weapon, info.timeManager())) {
            event.cancel();
        }
    }

    @Override
    public boolean isReadyToFire(final Weapon weapon, final TimeManager timeManager) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        final var timePassed = timeManager.getCurrentGameTime() - state.lastShotTimestamp;
        return timePassed >= attributes.timeBetweenShots;
    }

    private void fire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var chargeState = weapon.getState(ThrowableChargeModule.State.class);
        final var attributes = weapon.getAttributes(Attributes.class);
        final var attackAbility = info.attackAbility();
        final double flightSpeed = chargeState.chargeAmount * attributes.flightSpeedMult;

        state.velocity.set(attackAbility.targetPosition)
                      .sub(info.shooterTransform().position);
        if (state.velocity.lengthSquared() != 0) {
            state.velocity.normalize(flightSpeed);
        }

        final long flightTime = (long) (chargeState.chargeAmount * attributes.flightDurationMult);

        GrenadeArchetype.createGrenade(info.entities(),
                                       info.shooterTransform().position,
                                       state.velocity,
                                       attackAbility.projectileLayer,
                                       attributes.stats,
                                       attackAbility.damageSource,
                                       info.timeManager().getCurrentGameTime(),
                                       flightTime);
    }

    private void afterFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        state.lastShotTimestamp = info.timeManager().getCurrentGameTime();
    }

    public static class State {
        private final Vector2d velocity = new Vector2d();

        private long lastShotTimestamp = -1000;
    }

    public static record Attributes(
            long timeBetweenShots,
            GrenadeStats stats,
            double flightDurationMult,
            double flightSpeedMult
    ) {}
}
