package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.*;

/**
 * Overheat module that increases weapon's overheat while trigger is held down, and starts cooling down weapon once user
 * releases trigger. Once overheat reaches its maximum value, the weapon jams for a while.
 */
@Deprecated
public class OverheatFromTriggerDownModule implements WeaponModule<OverheatFromTriggerDownModule.Attributes> {
    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.weaponFire(this::checkIfCanFire, Phase.CHECK);
        hooks.weaponStateQuery(this::stateQuery, Phase.TRIGGER);
        hooks.triggerPull(this::triggerPull, Phase.TRIGGER);
        hooks.triggerRelease(this::triggerRelease, Phase.TRIGGER);
        hooks.weaponEquip(this::equip, Phase.CHECK);
        hooks.weaponUnequip(this::unequip, Phase.CHECK);

        hooks.registerStateFactory(State.class, State::new);
    }

    public void equip(
            final Weapon weapon,
            final WeaponEquipEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        updateHeatState(state, attributes, info.timeManager());
        state.isTriggerDown = false;
        state.heatAtTriggerRelease = state.heat;
        state.triggerReleaseTimestamp = info.timeManager().getCurrentGameTime();
    }

    public void unequip(
            final Weapon weapon,
            final WeaponUnequipEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        state.isTriggerDown = false;
        state.heatAtTriggerRelease = state.heat;
        state.triggerReleaseTimestamp = info.timeManager().getCurrentGameTime();
    }

    public void checkIfCanFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        updateHeatState(state, attributes, info.timeManager());
        if (state.isJammed || !state.isTriggerDown) {
            event.cancel();
        }
    }

    public void triggerPull(
            final Weapon weapon,
            final TriggerPullEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        updateHeatState(state, attributes, info.timeManager());
        state.isTriggerDown = true;
        state.heatAtTriggerPull = state.heat;
        state.triggerPullTimestamp = info.timeManager().getCurrentGameTime();
    }

    public void triggerRelease(
            final Weapon weapon,
            final TriggerReleaseEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

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
            final Weapon weapon,
            final WeaponStateQuery event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

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
