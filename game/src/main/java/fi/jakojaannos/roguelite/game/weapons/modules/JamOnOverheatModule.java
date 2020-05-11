package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.WeaponFireEvent;

/**
 * Module that prevents weapon from firing when overheat reaches its maximum threshold. Jammed weapon takes some time to
 * clear. Heat is set to 0 after jam clears. Requires {@link OverheatBaseModule}.
 */
public class JamOnOverheatModule implements WeaponModule<JamOnOverheatModule.Attributes>, HeatSource {

    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.weaponFire(this::checkIfCanFire, Phase.CHECK);
        hooks.weaponStateQuery(this::stateQuery, Phase.TRIGGER);
        hooks.registerStateFactory(State.class, State::new);
        hooks.postRegister(this::postRegister);
    }

    private void checkIfCanFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var baseState = weapon.getState(OverheatBaseModule.State.class);
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);
        updateJamState(baseState, state, attributes, weapon, info.timeManager());

        if (state.isJammed) {
            event.cancel();
        }
    }

    private void postRegister(final WeaponModules modules) {
        modules.require(OverheatBaseModule.class)
               .registerHeatSource(this);
    }

    private void stateQuery(
            final Weapon weapon,
            final WeaponStateQuery query,
            final ActionInfo info
    ) {
        final var baseState = weapon.getState(OverheatBaseModule.State.class);
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);
        updateJamState(baseState, state, attributes, weapon, info.timeManager());

        query.jammed = state.isJammed;
    }

    private void updateJamState(
            final OverheatBaseModule.State baseState,
            final State state,
            final Attributes attributes,
            final Weapon weapon,
            final TimeManager timeManager
    ) {
        if (timeManager.getCurrentGameTime() - state.jamStartTimestamp >= attributes.jamDuration) {
            state.isJammed = false;
        }

        if (baseState.getHeat(weapon, timeManager) >= attributes.maxHeat) {
            state.isJammed = true;
            state.jamStartTimestamp = timeManager.getCurrentGameTime();
        }
    }

    @Override
    public double getHeatDeltaSinceLastQuery(
            final Weapon weapon,
            final TimeManager timeManager
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);
        if (state.isJammed) {
            // "reset" heat to zero on overheat
            // FIXME: this could cause a bug, if this method is not called once during the jam (heat might not be set to 0)
            return -attributes.maxHeat;
        }
        return 0;
    }

    public static class State {
        private boolean isJammed;
        private long jamStartTimestamp;
    }

    public static record Attributes(
            double maxHeat,
            long jamDuration
    ) {}
}
