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
        if (state.ammo == attributes.magazineCapacity) {
            event.cancel();
        }
    }

    public void reload(
            final State state,
            final Attributes attributes,
            final ReloadEvent event,
            final ActionInfo info
    ) {
        state.ammo = attributes.magazineCapacity;
    }

    public void checkIfCanFire(
            final State state,
            final Attributes attributes,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        if (state.ammo <= 0) {
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
    }


    public void stateQuery(
            final State state,
            final Attributes attributes,
            final WeaponStateQuery event,
            final ActionInfo info
    ) {
        event.currentAmmo = state.ammo;
        event.maxAmmo = attributes.magazineCapacity;
    }

    public static class State {
        public int ammo;

        public State(final int currentAmmo) {
            this.ammo = currentAmmo;
        }
    }

    public static class Attributes {
        public int magazineCapacity = 30;
    }
}
