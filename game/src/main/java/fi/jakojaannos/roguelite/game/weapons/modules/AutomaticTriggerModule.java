package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.*;

public class AutomaticTriggerModule implements WeaponModule<NoAttributes> {
    @Override
    public void register(final WeaponHooks hooks, final NoAttributes ignored) {
        hooks.weaponFire(this::checkIfCanFire, Phase.CHECK);
        hooks.triggerPull(this::onTriggerPull, Phase.TRIGGER);
        hooks.triggerRelease(this::onTriggerRelease, Phase.TRIGGER);
        hooks.weaponEquip(this::equip, Phase.POST);
        hooks.weaponUnequip(this::unequip, Phase.POST);

        hooks.registerStateFactory(State.class, State::new);
    }

    public void onTriggerPull(final Weapon weapon, final TriggerPullEvent event, final ActionInfo info) {
        final var state = weapon.getState(State.class);
        state.triggerDown = true;
    }

    public void onTriggerRelease(final Weapon weapon, final TriggerReleaseEvent event, final ActionInfo info) {
        final var state = weapon.getState(State.class);
        state.triggerDown = false;
    }

    public void checkIfCanFire(final Weapon weapon, final WeaponFireEvent event, final ActionInfo info) {
        final var state = weapon.getState(State.class);
        if (!state.triggerDown) {
            event.cancel();
        }
    }

    public void equip(final Weapon weapon, final WeaponEquipEvent event, final ActionInfo info) {
        final var state = weapon.getState(State.class);
        state.triggerDown = false;
    }

    public void unequip(final Weapon weapon, final WeaponUnequipEvent event, final ActionInfo info) {
        final var state = weapon.getState(State.class);
        state.triggerDown = false;
    }

    public static class State {
        public boolean triggerDown;
    }
}
