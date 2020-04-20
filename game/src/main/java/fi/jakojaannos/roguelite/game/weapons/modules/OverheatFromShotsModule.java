package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.WeaponFireEvent;

/**
 * Overheat module that increases weapon's overheat from every shot fired, and cools it down every tick. Once overheat
 * reaches its maximum value, the weapon jams for a while.
 */
public class OverheatFromShotsModule implements WeaponModule<OverheatFromShotsModule.Attributes> {
    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.registerWeaponFire(this::checkIfCanFire, Phase.CHECK);
        hooks.registerWeaponFire(this::afterFire, Phase.POST);
        hooks.registerWeaponStateQuery(this::stateQuery, Phase.TRIGGER);

        hooks.registerStateFactory(State.class, State::new);
    }

    public void checkIfCanFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        updateHeatState(state, attributes, info.timeManager());
        if (state.isJammed) {
            event.cancel();
        }
    }

    public void afterFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        state.heat += attributes.heatPerShot;
        state.heatAtLastShot = state.heat;
        state.lastShotTimestamp = info.timeManager().getCurrentGameTime();
        // weapon can jam after firing
        updateHeatState(state, attributes, info.timeManager());
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
                state.heatAtLastShot = 0;
            }
        } else {
            final var timeSinceLastShot = timeManager.getCurrentGameTime() - state.lastShotTimestamp;
            final var heatDissipated = timeSinceLastShot * attributes.heatDissipationPerTick;
            state.heat = state.heatAtLastShot - heatDissipated;
            if (state.heat <= 0) {
                state.heat = 0;
            }
            if (state.heat >= attributes.maxHeat) {
                state.isJammed = true;
                state.jamStartTimestamp = timeManager.getCurrentGameTime();
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
        private double heatAtLastShot;
        private boolean isJammed;
        private long jamStartTimestamp;
        private long lastShotTimestamp;
    }

    public static record Attributes(
            double heatPerShot,
            double maxHeat,
            double heatDissipationPerTick,
            long jamDurationInTicks
    ) {}
}
