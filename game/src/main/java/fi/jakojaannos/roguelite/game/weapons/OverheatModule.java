package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

public class OverheatModule implements WeaponModule<OverheatModule.State, OverheatModule.Attributes> {
    @Override
    public State getDefaultState(final Attributes attributes) {
        return new State();
    }

    @Override
    public void register(final WeaponHooks hooks) {
        hooks.registerWeaponStateQuery(this, this::stateQuery, Phase.TRIGGER);
        hooks.registerWeaponFire(this, this::checkIfCanFire, Phase.CHECK);
        hooks.registerWeaponFire(this, this::afterFire, Phase.POST);
        hooks.registerTriggerRelease(this, this::triggerRelease, Phase.TRIGGER);
        hooks.registerTriggerPull(this, this::triggerPull, Phase.TRIGGER);
        //hooks.registerReload(this, this::checkIfCanReload, Phase.CHECK);
        //hooks.registerReload(this, this::afterReload, Phase.POST);
    }

    public void checkIfCanFire(
            final State state,
            final Attributes attributes,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        updateHeatState(state, attributes, info.timeManager());
        if (state.isJammed) {
            event.cancel();
        }
    }

    public void afterFire(
            final State state,
            final Attributes attributes,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        state.heat += attributes.heatPerShot;
        updateHeatState(state, attributes, info.timeManager());
    }

    public void triggerPull(
            final State state,
            final Attributes attributes,
            final TriggerPullEvent event,
            final ActionInfo info
    ) {
        state.isTriggerDown = true;
    }

    public void triggerRelease(
            final State state,
            final Attributes attributes,
            final TriggerReleaseEvent event,
            final ActionInfo info
    ) {
        state.isTriggerDown = false;
        state.triggerReleaseTimestamp = info.timeManager().getCurrentGameTime();
        state.heatAtTriggerRelease = state.heat;
    }

    private void updateHeatState(
            final State state,
            final Attributes attributes,
            final TimeManager timeManager
    ) {
        if (state.isJammed) {
            final var timeJammed = timeManager.getCurrentGameTime() - state.jamStartTimestamp;
            if (timeJammed >= attributes.jamDurationInTicks) {
                state.isJammed = false;
                state.heat = 0;
            }
        } else {
            if (state.isTriggerDown) {
                // in case trigger is held down: afterFire() handles increasing heat, we just check for overheat
                if (state.heat >= attributes.maxHeat) {
                    state.heat = attributes.maxHeat;
                    state.isJammed = true;
                    state.jamStartTimestamp = timeManager.getCurrentGameTime();
                }
            } else {
                // cooling down
                final var timeSinceTriggerRelease = timeManager.getCurrentGameTime() - state.triggerReleaseTimestamp;
                final var heatDissipated = timeSinceTriggerRelease * attributes.heatDissipationPerTick;

                state.heat = state.heatAtTriggerRelease - heatDissipated;
                if (state.heat <= 0) {
                    state.heat = 0;
                }
            }
        }
    }

    public void stateQuery(
            final State state,
            final Attributes attributes,
            final WeaponStateQuery event,
            final ActionInfo info
    ) {
        updateHeatState(state, attributes, info.timeManager());
        event.heat = state.heat;
        event.jammed = state.isJammed;
    }

    public static class State {
        private double heat;
        private double heatAtTriggerRelease;
        private long triggerReleaseTimestamp;
        private boolean isJammed;
        private long jamStartTimestamp;
        private boolean isTriggerDown;
    }

    public static record Attributes(
            double heatPerShot,
            double maxHeat,
            double heatDissipationPerTick,
            long jamDurationInTicks
    ) {}
}
