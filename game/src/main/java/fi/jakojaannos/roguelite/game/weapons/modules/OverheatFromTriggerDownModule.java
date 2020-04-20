package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.*;

/**
 * Overheat module that increases weapon's overheat while trigger is held down, and starts cooling down weapon once user
 * releases trigger. Once overheat reaches its maximum value, the weapon jams for a while.
 */
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
        hooks.registerWeaponEquip(this, this::equip, Phase.CHECK);
        hooks.registerWeaponUnequip(this, this::unequip, Phase.CHECK);
    }

    public void equip(
            final State state,
            final Attributes attributes,
            final WeaponEquipEvent event,
            final ActionInfo info
    ) {
        updateHeatState(state, attributes, info.timeManager());
        state.isTriggerDown = false;
        state.heatAtTriggerRelease = state.heat;
        state.triggerReleaseTimestamp = info.timeManager().getCurrentGameTime();
    }

    public void unequip(
            final State state,
            final Attributes attributes,
            final WeaponUnequipEvent event,
            final ActionInfo info
    ) {
        state.isTriggerDown = false;
        state.heatAtTriggerRelease = state.heat;
        state.triggerReleaseTimestamp = info.timeManager().getCurrentGameTime();
    }

    public void checkIfCanFire(
            final State state,
            final Attributes attributes,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        updateHeatState(state, attributes, info.timeManager());
        if (state.isJammed || !state.isTriggerDown) {
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
        state.isTriggerDown = true;
        state.heatAtTriggerPull = state.heat;
        state.triggerPullTimestamp = info.timeManager().getCurrentGameTime();
    }

    public void triggerRelease(
            final State state,
            final Attributes attributes,
            final TriggerReleaseEvent event,
            final ActionInfo info
    ) {
        updateHeatState(state, attributes, info.timeManager());
        state.isTriggerDown = false;
        state.heatAtTriggerRelease = state.heat;
        state.triggerReleaseTimestamp = info.timeManager().getCurrentGameTime();
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
                    state.isJammed = true;
                    state.heat = attributes.maxHeat;
                    state.jamStartTimestamp = timeManager.getCurrentGameTime();
                    // by having this line the weapon doesn't start shooting once jam has cleared (if user holds the trigger down)
                    state.isTriggerDown = false;
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
