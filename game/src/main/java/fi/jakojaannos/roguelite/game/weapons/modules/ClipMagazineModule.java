package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.events.GunshotEvent;
import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.ReloadEvent;
import fi.jakojaannos.roguelite.game.weapons.events.TriggerPullEvent;
import fi.jakojaannos.roguelite.game.weapons.events.WeaponFireEvent;
import fi.jakojaannos.roguelite.game.weapons.events.WeaponUnequipEvent;

public class ClipMagazineModule implements WeaponModule<ClipMagazineModule.Attributes> {
    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.weaponStateQuery(this::stateQuery, Phase.TRIGGER);
        hooks.reload(this::checkIfCanReload, Phase.CHECK);
        hooks.reload(this::reload, Phase.TRIGGER);
        hooks.weaponFire(this::checkIfCanFire, Phase.CHECK);
        hooks.weaponFire(this::afterFiring, Phase.POST);
        hooks.weaponUnequip(this::unequip, Phase.TRIGGER);

        hooks.triggerPull(this::afterTriggerPull, Phase.POST);

        hooks.registerStateFactory(State.class, () -> new State(attributes.magazineCapacity));
    }

    private void afterTriggerPull(
            final Weapon weapon,
            final TriggerPullEvent triggerPullEvent,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        if (state.ammo <= 0) {
            info.events().fire(new GunshotEvent(GunshotEvent.Variant.CLICK));
        }
    }

    public void checkIfCanReload(
            final Weapon weapon,
            final ReloadEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        updateReloadState(state, attributes, info.timeManager());
        if (state.isReloading || state.ammo == attributes.magazineCapacity) {
            event.cancel();
        }
    }

    public void reload(
            final Weapon weapon,
            final ReloadEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        state.isReloading = true;
        state.reloadStartTimestamp = info.timeManager().getCurrentGameTime();
    }

    public void checkIfCanFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        updateReloadState(state, attributes, info.timeManager());
        if (state.isReloading || state.ammo <= 0) {
            event.cancel();
        }
    }

    public void afterFiring(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        if (state.ammo > 0) {
            state.ammo--;
        }
    }

    public void unequip(
            final Weapon weapon,
            final WeaponUnequipEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        updateReloadState(state, attributes, info.timeManager());
        if (state.isReloading) {
            state.isReloading = false;
        }
    }

    public void stateQuery(
            final Weapon weapon,
            final WeaponStateQuery event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        updateReloadState(state, attributes, info.timeManager());
        if (state.isReloading) {
            event.currentAmmo = state.ammo;
            event.maxAmmo = 666;
        } else {
            event.currentAmmo = state.ammo;
            event.maxAmmo = attributes.magazineCapacity;
        }
    }

    private void updateReloadState(
            final State state,
            final Attributes attributes,
            final TimeManager timeManager
    ) {
        if (!state.isReloading) {
            return;
        }
        if (timeManager.getCurrentGameTime() - state.reloadStartTimestamp < attributes.reloadTime) {
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

    public static record Attributes(
            int magazineCapacity,
            long reloadTime
    ) {}
}
