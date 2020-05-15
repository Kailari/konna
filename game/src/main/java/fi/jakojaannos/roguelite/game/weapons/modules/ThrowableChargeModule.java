package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.game.weapons.*;

public class ThrowableChargeModule implements WeaponModule<ThrowableChargeModule.Attributes> {

    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.registerStateFactory(State.class, State::new);
    }

    public static class State {
        public double chargeAmount;
    }

    // TODO: change to NoAttributes
    public static record Attributes() {}
}
