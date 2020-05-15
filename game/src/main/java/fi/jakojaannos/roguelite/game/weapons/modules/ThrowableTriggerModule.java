package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.*;

/**
 * Trigger module for throwable weapons. Press and hold fire button to start charging the attack, release to fire.
 * Charge does not start before firing mechanism is ready to fire. Requires {@link ThrowableChargeModule}
 */
public class ThrowableTriggerModule implements WeaponModule<ThrowableTriggerModule.Attributes> {
    private final Class<? extends FiringModule>[] firingModuleClasses;
    private final FiringModule[] firingModules;

    /**
     * Takes a list of firing modules in this weapon: if any of them is not ready to fire when user pulls trigger, that
     * trigger pull does not start charging attack. If the weapon has multiple firing modules, only add the ones that
     * should block charge from starting
     *
     * @param firingModuleClasses list of firing modules in this weapon that can block charging
     */
    @SafeVarargs
    public ThrowableTriggerModule(final Class<? extends FiringModule>... firingModuleClasses) {
        this.firingModuleClasses = firingModuleClasses;
        this.firingModules = new FiringModule[firingModuleClasses.length];
    }

    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.weaponFire(this::checkIfCanFire, Phase.CHECK);
        hooks.weaponFire(this::postFire, Phase.POST);
        hooks.triggerPull(this::triggerPull, Phase.TRIGGER);
        hooks.triggerRelease(this::triggerRelease, Phase.TRIGGER);
        hooks.weaponEquip(this::equip, Phase.TRIGGER);
        hooks.weaponUnequip(this::unequip, Phase.TRIGGER);

        hooks.weaponStateQuery(this::stateQuery, Phase.TRIGGER);
        hooks.registerStateFactory(State.class, State::new);
        hooks.postRegister(this::postRegister);
    }

    private void equip(
            final Weapon weapon,
            final WeaponEquipEvent weaponEquipEvent,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        state.isTriggerPulled = false;
        state.shouldFire = false;
    }

    private void unequip(
            final Weapon weapon,
            final WeaponUnequipEvent weaponUnequipEvent,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        state.isTriggerPulled = false;
        state.shouldFire = false;
    }

    private void checkIfCanFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        if (state.hasFired) {
            event.cancel();
        }

        if (!state.shouldFire) {
            event.cancel();
        }

        if (state.triggerReleaseTimestamp != info.timeManager().getCurrentGameTime()) {
            event.cancel();
        }
    }

    private void postFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        state.hasFired = true;
        state.shouldFire = false;
    }

    private void triggerPull(
            final Weapon weapon,
            final TriggerPullEvent triggerPullEvent,
            final ActionInfo info
    ) {
        for (final var firingModule : this.firingModules) {
            if (!firingModule.isReadyToFire(weapon, info.timeManager())) {
                return;
            }
        }

        final var state = weapon.getState(State.class);
        state.triggerPullTimestamp = info.timeManager().getCurrentGameTime();
        state.isTriggerPulled = true;
        state.shouldFire = false;
        state.hasFired = false;
    }

    private void triggerRelease(
            final Weapon weapon,
            final TriggerReleaseEvent triggerReleaseEvent,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var chargeState = weapon.getState(ThrowableChargeModule.State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        // make sure weapon doesn't fire if user switches to weapon while holding fire key and then release it
        if (state.isTriggerPulled) {
            final var timeCharged = info.timeManager().getCurrentGameTime() - state.triggerPullTimestamp;

            if (timeCharged >= attributes.minChargeTimeInTicks) {
                chargeState.chargeAmount = Math.max(timeCharged, attributes.maxChargeTimeInTicks) * attributes.chargePerTick;
                state.shouldFire = true;
            }

            state.triggerReleaseTimestamp = info.timeManager().getCurrentGameTime();
            state.isTriggerPulled = false;
        }
    }

    private void stateQuery(
            final Weapon weapon,
            final WeaponStateQuery query,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        double charge;
        if (state.isTriggerPulled) {
            final var timeCharged = info.timeManager().getCurrentGameTime() - state.triggerPullTimestamp;
            charge = Math.min(timeCharged, attributes.maxChargeTimeInTicks);

            charge = charge / attributes.maxChargeTimeInTicks * 100;
        } else {
            charge = 0;
        }

        // FIXME: storing charge amount in heat while UI is WIP
        query.heat = charge;
    }

    private void postRegister(final WeaponModules modules) {
        for (int i = 0; i < this.firingModuleClasses.length; i++) {
            this.firingModules[i] = modules.require(this.firingModuleClasses[i]);
        }
    }

    public static class State {
        private boolean shouldFire;
        private boolean hasFired;
        private boolean isTriggerPulled;

        private long triggerPullTimestamp;
        private long triggerReleaseTimestamp;
    }

    public static record Attributes(
            double chargePerTick,
            long minChargeTimeInTicks,
            long maxChargeTimeInTicks
    ) {}
}
