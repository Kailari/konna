package fi.jakojaannos.roguelite.game.weapons;

public class AutomaticTriggerModule implements WeaponModule<AutomaticTriggerState, NoAttributes> {
    @Override
    public AutomaticTriggerState getState(final InventoryWeapon weapon) {
        return weapon.getState().getOrCreateState(AutomaticTriggerModule.class, AutomaticTriggerState::new);
    }

    @Override
    public NoAttributes getAttributes(final InventoryWeapon weapon) {
        return NoAttributes.INSTANCE;
    }

    @Override
    public void register(final WeaponHooks hooks) {
        hooks.onWeaponFire(this, this::checkIfCanFire, Phase.CHECK);
        hooks.onTriggerPull(this, this::onTriggerPull, Phase.TRIGGER);
        hooks.onTriggerRelease(this, this::onTriggerRelease, Phase.TRIGGER);
    }

    public void onTriggerPull(
            final AutomaticTriggerState state,
            final NoAttributes attributes,
            final TriggerPullEvent event,
            final ActionInfo info
    ) {
        state.triggerDown = true;
        event.tryShoot();
    }

    public void onTriggerRelease(
            final AutomaticTriggerState state,
            final NoAttributes attributes,
            final TriggerReleaseEvent event,
            final ActionInfo info
    ) {
        state.triggerDown = false;
    }

    public void checkIfCanFire(
            final AutomaticTriggerState state,
            final NoAttributes attributes,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        if (!state.triggerDown) {
            event.cancel();
        }
    }

}
