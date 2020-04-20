package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.*;

public class SingleShotTriggerModule implements WeaponModule<NoAttributes> {
    @Override
    public void register(final WeaponHooks hooks, final NoAttributes ignored) {
        hooks.registerWeaponFire(this::checkIfCanFire, Phase.CHECK);
        hooks.registerTriggerPull(this::onTriggerPull, Phase.TRIGGER);
        hooks.registerTriggerRelease(this::onTriggerRelease, Phase.TRIGGER);
        hooks.registerWeaponEquip(this::equip, Phase.POST);
        hooks.registerWeaponUnequip(this::unequip, Phase.POST);

        hooks.registerStateFactory(State.class, State::new);
    }

    public void onTriggerPull(
            final Weapon weapon,
            final TriggerPullEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        state.triggerDown = true;
    }

    public void onTriggerRelease(
            final Weapon weapon,
            final TriggerReleaseEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        state.triggerDown = false;
        state.assumeShotFired = false;
    }

    public void checkIfCanFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        if (!state.triggerDown || state.assumeShotFired) {
            event.cancel();
            return;
        }
        // note: we are not setting this with "hooks.registerWeaponFire/POST" as this would not lead to wanted behaviour
        state.assumeShotFired = true;
    }

    public void equip(
            final Weapon weapon,
            final WeaponEquipEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        state.triggerDown = false;
        state.assumeShotFired = false;
    }

    public void unequip(
            final Weapon weapon,
            final WeaponUnequipEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        state.triggerDown = false;
        state.assumeShotFired = false;
    }

    public static class State {
        public boolean triggerDown;
        public boolean assumeShotFired;
    }
}
