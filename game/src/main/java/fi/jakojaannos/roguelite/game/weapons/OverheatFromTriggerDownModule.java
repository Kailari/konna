package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

public class OverheatFromTriggerDownModule implements WeaponModule<OverheatFromTriggerDownModule.State, OverheatFromTriggerDownModule.Attributes> {
    @Override
    public State getDefaultState(final Attributes attributes) {
        return new State();
    }

    @Override
    public void register(final WeaponHooks hooks) {
        hooks.registerWeaponFire(this, this::checkIfCanFire, Phase.CHECK);
        hooks.registerWeaponStateQuery(this, this::stateQuery, Phase.TRIGGER);
        hooks.registerTriggerPull(this, this::triggerPull, Phase.TRIGGER);
        hooks.registerTriggerRelease(this, this::triggerRelease, Phase.TRIGGER);
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

    public void triggerPull(
            final State state,
            final Attributes attributes,
            final TriggerPullEvent event,
            final ActionInfo info
    ) {
        updateHeatState(state, attributes, info.timeManager());
        state.triggerPullTimestamp = info.timeManager().getCurrentGameTime();
        state.heatAtTriggerPull = state.heat;
        state.isTriggerDown = true;
    }

    public void triggerRelease(
            final State state,
            final Attributes attributes,
            final TriggerReleaseEvent event,
            final ActionInfo info
    ) {
        updateHeatState(state, attributes, info.timeManager());
        state.triggerReleaseTimestamp = info.timeManager().getCurrentGameTime();
        state.heatAtTriggerRelease = state.heat;
        state.isTriggerDown = false;
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
                // heating up
                final var timeTriggerDown = timeManager.getCurrentGameTime() - state.triggerPullTimestamp;
                final var heatGathered = attributes.heatPerTick * timeTriggerDown;
                state.heat = state.heatAtTriggerPull + heatGathered;

                if (state.heat >= attributes.maxHeat) {
                    state.heat = attributes.maxHeat;
                    state.isJammed = true;
                    state.jamStartTimestamp = timeManager.getCurrentGameTime();
                }
            } else {
                // cooling down
                final var timeTriggerDown = timeManager.getCurrentGameTime() - state.triggerReleaseTimestamp;
                final var heatDissipated = attributes.heatDissipationPerTick * timeTriggerDown;
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
        private double heatAtTriggerPull;
        private double heatAtTriggerRelease;
        private long triggerPullTimestamp;
        private long triggerReleaseTimestamp;
        private boolean isJammed;
        private long jamStartTimestamp;
        private boolean isTriggerDown;
    }

    public static record Attributes(
            double heatPerTick,
            double maxHeat,
            double heatDissipationPerTick,
            long jamDurationInTicks
    ) {}
}
