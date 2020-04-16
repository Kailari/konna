package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

public class ClipMagazineModule implements WeaponModule<ClipMagazineModule.State, ClipMagazineModule.Attributes> {

    @Override
    public State getDefaultState(final Attributes attributes) {
        return new State(attributes.magazineCapacity);
    }

    @Override
    public void register(final WeaponHooks hooks) {
        hooks.registerWeaponStateQuery(this, this::stateQuery, Phase.TRIGGER);
        hooks.registerReload(this, this::checkIfCanReload, Phase.CHECK);
        hooks.registerReload(this, this::reload, Phase.TRIGGER);
        hooks.registerWeaponFire(this, this::checkIfCanFire, Phase.CHECK);
        hooks.registerWeaponFire(this, this::afterFiring, Phase.POST);
        hooks.registerWeaponUnequip(this, this::unequip, Phase.TRIGGER);
    }

    public void checkIfCanReload(
            final State state,
            final Attributes attributes,
            final ReloadEvent event,
            final ActionInfo info
    ) {
        updateAmmoState(state, attributes, info.timeManager());
        if (state.isReloading
            || state.ammo == attributes.magazineCapacity) {
            event.cancel();
        }
    }

    public void reload(
            final State state,
            final Attributes attributes,
            final ReloadEvent event,
            final ActionInfo info
    ) {
        state.isReloading = true;
        state.reloadStartTimestamp = info.timeManager().getCurrentGameTime();
    }

    public void checkIfCanFire(
            final State state,
            final Attributes attributes,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        updateAmmoState(state, attributes, info.timeManager());
        if (state.isReloading
            || state.ammo <= 0) {
            event.cancel();
        }
    }

    public void afterFiring(
            final State state,
            final Attributes attributes,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        if (state.ammo > 0) {
            state.ammo--;
        }
    }

    public void unequip(
            final State state,
            final Attributes attributes,
            final WeaponUnequipEvent event,
            final ActionInfo info
    ) {
        updateAmmoState(state, attributes, info.timeManager());
        if (state.isReloading) {
            state.isReloading = false;
        }
    }

    public void stateQuery(
            final State state,
            final Attributes attributes,
            final WeaponStateQuery event,
            final ActionInfo info
    ) {
        updateAmmoState(state, attributes, info.timeManager());
        if (state.isReloading) {
            event.currentAmmo = state.ammo;
            event.maxAmmo = 666;
        } else {
            event.currentAmmo = state.ammo;
            event.maxAmmo = attributes.magazineCapacity;
        }
    }

    private void updateAmmoState(
            final State state,
            final Attributes attributes,
            final TimeManager timeManager
    ) {
        if (!state.isReloading) {
            return;
        }
        if (timeManager.getCurrentGameTime() - state.reloadStartTimestamp <= attributes.reloadTime) {
            return;
        }

        state.ammo = attributes.magazineCapacity;
        state.isReloading = false;
    }

    public static class State {
        public int ammo;
        public boolean isReloading;
        public long reloadStartTimestamp;

        public State(final int currentAmmo) {
            this.ammo = currentAmmo;
        }
    }

    public static class Attributes {
        public int magazineCapacity = 30;
        public long reloadTime = 60;

        public Attributes(final int magazineCapacity, final long reloadTimeInTicks) {
            this.magazineCapacity = magazineCapacity;
            this.reloadTime = reloadTimeInTicks;
        }
    }
}
