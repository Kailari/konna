package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.weapons.*;

public class HeatSinkModule implements WeaponModule<HeatSinkModule.Attributes>, HeatSource {


    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.registerStateFactory(State.class, State::new);

        hooks.postRegister(this::postRegister);
    }

    private void postRegister(final WeaponModules modules) {
        final var base = modules.require(OverheatBaseModule.class);
        base.registerHeatSource(this);
    }

    @Override
    public double getHeatDeltaSinceLastQuery(final Weapon weapon, final TimeManager timeManager) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        if (state.firstQuery) {
            state.firstQuery = false;
            state.lastQueryTimeStamp = 0;
            return 0;
        }

        final var timePassed = timeManager.getCurrentGameTime() - state.lastQueryTimeStamp;
        state.lastQueryTimeStamp = timeManager.getCurrentGameTime();
        return -timePassed * attributes.heatDissipationPerTick;
    }

    public static class State {
        private boolean firstQuery = true;
        private long lastQueryTimeStamp;
    }

    public static record Attributes(
            double heatDissipationPerTick,
            boolean coolOnlyOnTriggerReleased, // TODO: implement these
            boolean coolOnlyWhenEquipped
    ) {
    }
}
