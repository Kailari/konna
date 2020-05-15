package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.game.weapons.*;

/**
 * Module used for storing charge for various throwable weapons.
 */
public class ThrowableChargeModule implements WeaponModule<ThrowableChargeModule.Attributes> {

    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.registerStateFactory(State.class, () -> new State(attributes.defaultCharge));
    }

    public static class State {
        public double chargeAmount;

        public State(final double defaultCharge) {
            this.chargeAmount = defaultCharge;
        }
    }

    public static record Attributes(
            double defaultCharge
    ) {}
}
