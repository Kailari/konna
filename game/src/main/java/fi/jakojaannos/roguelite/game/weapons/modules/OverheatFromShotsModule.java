package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.WeaponFireEvent;

/**
 * Module that adds a fixed amount of heat (as defined in {@link Attributes}) after every shot fired. Requires {@link
 * OverheatBaseModule}.
 */
public class OverheatFromShotsModule implements WeaponModule<OverheatFromShotsModule.Attributes>, HeatSource {
    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.weaponFire(this::afterFire, Phase.POST);

        hooks.postRegister(this::postRegister);
        hooks.registerStateFactory(State.class, State::new);
    }

    private void postRegister(final WeaponModules modules) {
        modules.require(OverheatBaseModule.class)
               .registerHeatSource(this);
    }

    public void afterFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        state.heatSinceLastQuery += attributes.heatPerShot;
    }

    @Override
    public double getHeatDeltaSinceLastQuery(
            final Weapon weapon,
            final TimeManager timeManager
    ) {
        final var state = weapon.getState(State.class);
        final var delta = state.heatSinceLastQuery;
        state.heatSinceLastQuery = 0;

        return delta;
    }

    public static class State {
        double heatSinceLastQuery;
    }

    public static record Attributes(
            double heatPerShot
    ) {}
}
