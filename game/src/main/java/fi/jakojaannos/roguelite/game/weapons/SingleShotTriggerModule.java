package fi.jakojaannos.roguelite.game.weapons;

public class SingleShotTriggerModule implements WeaponModule<SingleShotTriggerModule.State, NoAttributes> {

    @Override
    public State getDefaultState(final NoAttributes attributes) {
        return new State();
    }

    @Override
    public void register(final WeaponHooks hooks) {
        hooks.registerWeaponFire(this, this::checkIfCanFire, Phase.CHECK);
        hooks.registerTriggerPull(this, this::onTriggerPull, Phase.TRIGGER);
        hooks.registerTriggerRelease(this, this::onTriggerRelease, Phase.TRIGGER);
        hooks.registerWeaponEquip(this, this::equip, Phase.POST);
        hooks.registerWeaponUnequip(this, this::unequip, Phase.POST);
    }

    public void onTriggerPull(
            final State state,
            final NoAttributes attributes,
            final TriggerPullEvent event,
            final ActionInfo info
    ) {
        state.triggerDown = true;
    }

    public void onTriggerRelease(
            final State state,
            final NoAttributes attributes,
            final TriggerReleaseEvent event,
            final ActionInfo info
    ) {
        state.triggerDown = false;
        state.assumeShotFired = false;
    }

    public void checkIfCanFire(
            final State state,
            final NoAttributes attributes,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        if (!state.triggerDown || state.assumeShotFired) {
            event.cancel();
            return;
        }
        // note: we are not setting this with "hooks.registerWeaponFire/POST" as this would not lead to wanted behaviour
        state.assumeShotFired = true;
    }

    public void equip(
            final State state,
            final NoAttributes attributes,
            final WeaponEquipEvent event,
            final ActionInfo info
    ) {
        state.triggerDown = false;
        state.assumeShotFired = false;
    }

    public void unequip(
            final State state,
            final NoAttributes attributes,
            final WeaponUnequipEvent event,
            final ActionInfo info
    ) {
        state.triggerDown = false;
        state.assumeShotFired = false;
    }

    public static class State {
        public boolean triggerDown;
        public boolean assumeShotFired;
    }
}
