package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.*;

/**
 * Requires {@link ThrowableChargeModule}
 */
public class ThrowableFiringModule implements WeaponModule<ThrowableFiringModule.Attributes> {

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
    }

    private void equip(
            final Weapon weapon,
            final WeaponEquipEvent weaponEquipEvent,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        state.isTriggerPulled = false;
        state.shouldFire = false;
    }

    private void unequip(
            final Weapon weapon,
            final WeaponUnequipEvent weaponUnequipEvent,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        state.isTriggerPulled = false;
        state.shouldFire = false;
    }

    private void checkIfCanFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        if (state.hasFired) {
            event.cancel();
        }

        if (!state.shouldFire) {
            event.cancel();
        }

        final var timeSinceRelease = info.timeManager().getCurrentGameTime() - state.triggerReleaseTimestamp;
        if (timeSinceRelease > attributes.gracePeriodAfterRelease) {
            event.cancel();
        }
    }

    private void postFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        state.hasFired = true;
        state.shouldFire = false;
    }

    private void triggerPull(
            final Weapon weapon,
            final TriggerPullEvent triggerPullEvent,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

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

        // this check is to make sure weapon doesn't fire if user switches to weapon while holding fire key and then release it
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
            long maxChargeTimeInTicks,
            // if weapon isn't immediately ready after releasing trigger, allow firing for this many ticks
            long gracePeriodAfterRelease
    ) {}
}
