package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.TriggerPullEvent;
import fi.jakojaannos.roguelite.game.weapons.events.WeaponFireEvent;

public class RechargingMagazineModule implements WeaponModule<RechargingMagazineModule.Attributes> {

    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.weaponFire(this::checkIfCanFire, Phase.CHECK);
        hooks.weaponFire(this::afterFire, Phase.POST);
        hooks.weaponStateQuery(this::stateQuery, Phase.TRIGGER);
        hooks.triggerPull(this::preTriggerPull, Phase.CHECK);

        hooks.registerStateFactory(State.class, () -> new State(attributes.magazineCapacity));
    }

    /*
    This method is to stop ChargedTrigger from starting the charge if the magazine is empty
    (as in user presses the fire button with empty magazine).
     */
    private void preTriggerPull(final Weapon weapon, final TriggerPullEvent event, final ActionInfo info) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        updateAmmoState(state, attributes, info);

        if (state.ammoLeft <= 0) {
            event.cancel();
        }
    }

    private void checkIfCanFire(final Weapon weapon, final WeaponFireEvent event, final ActionInfo info) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        updateAmmoState(state, attributes, info);

        if (state.ammoLeft <= 0) {
            event.cancel();
        }
    }

    private void afterFire(final Weapon weapon, final WeaponFireEvent event, final ActionInfo info) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        if (state.ammoLeft == attributes.magazineCapacity) {
            state.reloadStart = info.timeManager().getCurrentGameTime();
            state.isReloading = true;
        }

        state.ammoLeft--;

        if (state.ammoLeft < 0) {
            state.ammoLeft = 0;
        }
    }

    private void updateAmmoState(final State state, final Attributes attributes, final ActionInfo info) {
        if (attributes.magazineCapacity <= 0) {
            return;
        }

        if (!state.isReloading) {
            return;
        }

        var timeReloaded = info.timeManager().getCurrentGameTime() - state.reloadStart;

        while (timeReloaded >= attributes.ammoRechargeTime) {
            // this feels illegal
            state.reloadStart += attributes.ammoRechargeTime;
            state.ammoLeft++;

            timeReloaded = info.timeManager().getCurrentGameTime() - state.reloadStart;

            if (state.ammoLeft >= attributes.magazineCapacity) {
                state.ammoLeft = attributes.magazineCapacity;
                state.isReloading = false;
                break;
            }
        }
    }

    private void stateQuery(final Weapon weapon, final WeaponStateQuery query, final ActionInfo info) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);
        updateAmmoState(state, attributes, info);

        query.currentAmmo = state.ammoLeft;

        // maxAmmo is used to display ammo recharge status
        if (state.isReloading) {
            final var timeReloaded = info.timeManager().getCurrentGameTime() - state.reloadStart;
            query.maxAmmo = (int) (timeReloaded * 100.0 / attributes.ammoRechargeTime);
        } else {
            query.maxAmmo = 0;
        }
    }

    public static class State {
        private int ammoLeft;
        private long reloadStart;
        private boolean isReloading;

        public State(final int ammo) {
            this.ammoLeft = ammo;
        }
    }

    public static record Attributes(
            int magazineCapacity,
            long ammoRechargeTime
    ) {}
}
