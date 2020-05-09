package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.TriggerPullEvent;
import fi.jakojaannos.roguelite.game.weapons.events.TriggerReleaseEvent;
import fi.jakojaannos.roguelite.game.weapons.events.WeaponUnequipEvent;

public class OverheatFromTriggerDownModule implements WeaponModule<OverheatFromTriggerDownModule.Attributes>, HeatSource {
    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.triggerPull(this::triggerPull, Phase.TRIGGER);
        hooks.triggerRelease(this::triggerRelease, Phase.TRIGGER);
        hooks.weaponUnequip(this::unequip, Phase.TRIGGER);

        hooks.registerStateFactory(State.class, State::new);
        hooks.postRegister(this::postRegister);
    }

    private void postRegister(
            final WeaponModules modules
    ) {
        modules.require(OverheatBaseModule.class)
               .registerHeatSource(this);
    }

    public void triggerPull(
            final Weapon weapon,
            final TriggerPullEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        state.isTriggerDown = true;
        state.triggerPullTimestamp = info.timeManager().getCurrentGameTime();
    }

    public void triggerRelease(
            final Weapon weapon,
            final TriggerReleaseEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        state.isTriggerDown = false;
        updateAccumulated(state, attributes, info.timeManager());
    }

    public void unequip(
            final Weapon weapon,
            final WeaponUnequipEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        state.isTriggerDown = false;
        updateAccumulated(state, attributes, info.timeManager());
    }

    private void updateAccumulated(
            final State state,
            final Attributes attributes,
            final TimeManager timeManager
    ) {

        if (state.firstQuery) {
            state.firstQuery = false;
            return;
        }

        if (!state.isTriggerDown) {
            return;
        }

        final var latest = Math.max(state.lastQueryTimestamp, state.triggerPullTimestamp);
        state.accumulated = (timeManager.getCurrentGameTime() - latest) * attributes.heatPerTick;
    }

    @Override
    public double getHeatDeltaSinceLastQuery(
            final Weapon weapon,
            final TimeManager timeManager
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        updateAccumulated(state, attributes, timeManager);

        final var delta = state.accumulated;
        state.accumulated = 0;
        state.lastQueryTimestamp = timeManager.getCurrentGameTime();
        return delta;
    }

    public static class State {
        private double accumulated;
        private long lastQueryTimestamp;
        private long triggerPullTimestamp;

        private boolean firstQuery;
        private boolean isTriggerDown;
    }

    public static record Attributes(
            double heatPerTick
    ) {}
}
