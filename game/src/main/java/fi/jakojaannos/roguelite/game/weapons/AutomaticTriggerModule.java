package fi.jakojaannos.roguelite.game.weapons;

public class AutomaticTriggerModule implements WeaponModule<AutomaticTriggerModule.State, NoAttributes> {
    @Override
    public State getDefaultState() {
        return new State();
    }

    @Override
    public void register(final WeaponHooks hooks) {
        hooks.registerWeaponFire(this, this::checkIfCanFire, Phase.CHECK);
        hooks.registerTriggerPull(this, this::onTriggerPull, Phase.TRIGGER);
        hooks.registerTriggerRelease(this, this::onTriggerRelease, Phase.TRIGGER);
    }

    public void onTriggerPull(
            final State state,
            final NoAttributes attributes,
            final TriggerPullEvent event,
            final ActionInfo info
    ) {
        state.triggerDown = true;
        event.tryShoot();
    }

    public void onTriggerRelease(
            final State state,
            final NoAttributes attributes,
            final TriggerReleaseEvent event,
            final ActionInfo info
    ) {
        state.triggerDown = false;
    }

    public void checkIfCanFire(
            final State state,
            final NoAttributes attributes,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        if (!state.triggerDown) {
            event.cancel();
        }
    }

    public static class State {
        public boolean triggerDown;
    }
}
