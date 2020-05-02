package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.*;

public class ChargedTriggerModule implements WeaponModule<ChargedTriggerModule.Attributes> {

    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.weaponFire(this::checkIfCanFire, Phase.CHECK);
        hooks.weaponFire(this::afterFire, Phase.POST);
        hooks.triggerPull(this::onTriggerPull, Phase.TRIGGER);
        hooks.triggerRelease(this::onTriggerRelease, Phase.TRIGGER);
        hooks.weaponEquip(this::onEquip, Phase.TRIGGER);
        hooks.weaponUnequip(this::onUnequip, Phase.TRIGGER);
        hooks.weaponStateQuery(this::stateQuery, Phase.TRIGGER);

        hooks.registerStateFactory(State.class, State::new);
    }

    private void checkIfCanFire(final Weapon weapon, final WeaponFireEvent event, final ActionInfo info) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        if (!state.isCharging) {
            event.cancel();
            return;
        }

        if (state.hasFired) {
            event.cancel();
            return;
        }

        final var timeCharged = info.timeManager().getCurrentGameTime() - state.chargeStartTimestamp;
        if (timeCharged < attributes.chargeTimeInTicks) {
            event.cancel();
        }
    }

    private void afterFire(final Weapon weapon, final WeaponFireEvent event, final ActionInfo info) {
        final var state = weapon.getState(State.class);
        state.hasFired = true;
    }

    private void onTriggerPull(final Weapon weapon, final TriggerPullEvent event, final ActionInfo info) {
        final var state = weapon.getState(State.class);

        state.isCharging = true;
        state.hasFired = false;
        state.chargeStartTimestamp = info.timeManager().getCurrentGameTime();
    }

    private void onTriggerRelease(final Weapon weapon, final TriggerReleaseEvent event, final ActionInfo info) {
        final var state = weapon.getState(State.class);

        state.hasFired = false;
        state.isCharging = false;
    }


    private void onEquip(final Weapon weapon, final WeaponEquipEvent event, final ActionInfo info) {
        final var state = weapon.getState(State.class);

        state.isCharging = false;
        state.hasFired = false;
    }

    private void onUnequip(final Weapon weapon, final WeaponUnequipEvent event, final ActionInfo info) {
        final var state = weapon.getState(State.class);

        state.isCharging = false;
        state.hasFired = false;
    }


    private void stateQuery(final Weapon weapon, final WeaponStateQuery query, final ActionInfo info) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        if (state.hasFired) {
            query.heat = 100.0;
            return;
        }

        if (!state.isCharging) {
            query.heat = 0;
            return;
        }

        // storing charge amount into "heat" variable for now, as weapon UI is kind of WIP
        query.heat = (info.timeManager().getCurrentGameTime()
                      - state.chargeStartTimestamp)
                     * 100.0 / attributes.chargeTimeInTicks;
    }

    public static class State {
        private boolean isCharging;
        private boolean hasFired;
        private long chargeStartTimestamp;
    }

    public static record Attributes(
            long chargeTimeInTicks
    ) {}
}
