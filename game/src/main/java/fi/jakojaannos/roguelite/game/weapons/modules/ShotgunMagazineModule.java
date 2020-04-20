package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.events.render.GunshotEvent;
import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.ReloadEvent;
import fi.jakojaannos.roguelite.game.weapons.events.TriggerPullEvent;
import fi.jakojaannos.roguelite.game.weapons.events.WeaponFireEvent;
import fi.jakojaannos.roguelite.game.weapons.events.WeaponUnequipEvent;

public class ShotgunMagazineModule implements WeaponModule<ShotgunMagazineModule.Attributes> {
    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.registerWeaponStateQuery(this::stateQuery, Phase.TRIGGER);
        hooks.registerReload(this::checkIfCanReload, Phase.CHECK);
        hooks.registerReload(this::reload, Phase.TRIGGER);
        hooks.registerWeaponFire(this::checkIfCanFire, Phase.CHECK);
        hooks.registerWeaponFire(this::afterFiring, Phase.POST);
        hooks.registerWeaponUnequip(this::unequip, Phase.TRIGGER);

        hooks.registerTriggerPull(this::afterTriggerPull, Phase.POST);

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

    public void checkIfCanFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        updateReloadState(state, attributes, info.timeManager());
        if (state.ammo <= 0) {
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
        if (state.isReloading) {
            state.isReloading = false;
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
        if (state.isReloading
            || state.ammo == attributes.magazineCapacity) {
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
        state.ammoOnReloadStart = state.ammo;
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

        final var oldAmmo = state.ammo;
        updateReloadState(state, attributes, info.timeManager());
        if (oldAmmo != state.ammo) {
            info.events().fire(new GunshotEvent(GunshotEvent.Variant.SHOTGUN_RELOAD));
        }

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
        if (attributes.reloadTime == 0) {
            // reloadTime = 0 -> congratulations, you have successfully turned shotgun magazine into AR magazine
            state.isReloading = false;
            state.ammo = attributes.magazineCapacity;
            return;
        }

        final long timeUsedReloading = timeManager.getCurrentGameTime() - state.reloadStartTimestamp;
        final int shellsReloaded = (int) (timeUsedReloading / attributes.reloadTime);

        state.ammo = state.ammoOnReloadStart + shellsReloaded;
        if (state.ammo >= attributes.magazineCapacity) {
            state.ammo = attributes.magazineCapacity;
            state.isReloading = false;
        }
    }

    public static class State {
        private int ammo;
        private boolean isReloading;
        private long reloadStartTimestamp;
        private int ammoOnReloadStart;

        public State(final int currentAmmo) {
            this.ammo = currentAmmo;
        }
    }

    public static record Attributes(
            int magazineCapacity,
            long reloadTime
    ) {}
}
