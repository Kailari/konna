package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.TriggerPullEvent;
import fi.jakojaannos.roguelite.game.weapons.events.TriggerReleaseEvent;
import fi.jakojaannos.roguelite.game.weapons.events.WeaponEquipEvent;
import fi.jakojaannos.roguelite.game.weapons.events.WeaponUnequipEvent;

/**
 * Module that passively cools down weapon. Requires {@link OverheatBaseModule} to be in the weapon.
 */
public class HeatSinkModule implements WeaponModule<HeatSinkModule.Attributes>, HeatSource {
    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.weaponEquip(this::equip, Phase.TRIGGER);
        hooks.weaponUnequip(this::unequip, Phase.TRIGGER);
        hooks.triggerPull(this::triggerPull, Phase.TRIGGER);
        hooks.triggerRelease(this::triggerRelease, Phase.TRIGGER);
        hooks.registerStateFactory(State.class, State::new);

        hooks.postRegister(this::postRegister);
    }

    private void postRegister(final WeaponModules modules) {
        modules.require(OverheatBaseModule.class)
               .registerHeatSource(this);
    }

    private void triggerPull(
            final Weapon weapon,
            final TriggerPullEvent triggerPullEvent,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);
        state.isTriggerDown = true;
        updateAccumulatedCooling(state, attributes, info.timeManager());
    }

    private void triggerRelease(
            final Weapon weapon,
            final TriggerReleaseEvent triggerReleaseEvent,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        state.isTriggerDown = false;
        state.triggerReleaseTimestamp = info.timeManager().getCurrentGameTime();
    }

    private void equip(
            final Weapon weapon,
            final WeaponEquipEvent weaponEquipEvent,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        state.isEquipped = true;
        state.equipTimestamp = info.timeManager().getCurrentGameTime();
    }

    private void unequip(
            final Weapon weapon,
            final WeaponUnequipEvent weaponUnequipEvent,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);
        state.isEquipped = false;
        state.isTriggerDown = false;

        updateAccumulatedCooling(state, attributes, info.timeManager());
    }


    /**
     * Gets accumulated cooling since last query/weapon equip/trigger release, and stores it in state. Sets {@code
     * state.lastQueryTimeStamp} to current game tick.
     *
     * @param state       State
     * @param attributes  Attributes
     * @param timeManager TimeManager
     */
    private void updateAccumulatedCooling(
            final State state,
            final Attributes attributes,
            final TimeManager timeManager
    ) {
        var latest = state.lastQueryTimeStamp;
        state.lastQueryTimeStamp = timeManager.getCurrentGameTime();

        if (attributes.coolOnlyWhenEquipped) {
            if (!state.isEquipped) {
                return;
            }
            latest = Math.max(latest, state.equipTimestamp);
        }

        if (attributes.coolOnlyWhenTriggerReleased) {
            if (state.isTriggerDown) {
                return;
            }
            latest = Math.max(latest, state.triggerReleaseTimestamp);
        }

        state.accumulated += (timeManager.getCurrentGameTime() - latest) * attributes.heatDissipationPerTick;
    }

    @Override
    public double getHeatDeltaSinceLastQuery(final Weapon weapon, final TimeManager timeManager) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);
        updateAccumulatedCooling(state, attributes, timeManager);

        final var delta = state.accumulated;
        state.accumulated = 0;
        return -delta;
    }

    public static class State {
        private double accumulated;

        private long lastQueryTimeStamp;
        private long triggerReleaseTimestamp;
        private long equipTimestamp;

        private boolean isEquipped;
        private boolean isTriggerDown;
    }

    public static record Attributes(
            double heatDissipationPerTick,
            boolean coolOnlyWhenTriggerReleased,
            boolean coolOnlyWhenEquipped
    ) {}
}
