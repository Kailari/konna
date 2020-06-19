package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.riista.utilities.TimeManager;
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
     * <p>
     * Brief explanation on how accumulated cooling is calculated:<br> Weapon can start cooling after certain events
     * (assuming the corresponding attribute is set, otherwise we ignore that event). These events can be:
     * <ul>
     *     <li>trigger is released</li>
     *     <li>weapon is equipped</li>
     *     <li>last query</li>
     * </ul>
     * We get the most recent of these events and calculate amount of ticks passed and multiply that by cooling per tick,
     * as specified in attributes. If weapon is in a state where it can't be cooling (for example weapon is unequipped
     * while {@code coolOnlyWhenEquipped = true}) then nothing is added to accumulated cooling.
     * <p>
     * As certain events can interrupt weapon cooling, this method should be called when starting those events.
     * These events are:
     * <ul>
     *     <li>unequipping weapon</li>
     *     <li>pulling trigger</li>
     * </ul>
     *
     * @param state       state of the module
     * @param attributes  module attributes
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
                // weapon is in state where it can't be cooled
                return;
            }
            latest = Math.max(latest, state.equipTimestamp);
        }

        if (attributes.coolOnlyWhenTriggerReleased) {
            if (state.isTriggerDown) {
                // weapon is in state where it can't be cooled
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
