package fi.jakojaannos.roguelite.game.weapons;

public class GrenadeWeapon implements Weapon<
        EndlessMagazineHandler.EndlessMagazineState,
        HoldToChargeTriggerState,
        HoldToChargeTriggerState> {

    private final EndlessMagazineHandler magazine;
    private final HoldToChargeTrigger trigger;
    private final ChargedFiringMechanism firing;

    public GrenadeWeapon() {
        this.magazine = new EndlessMagazineHandler();
        final var state = new HoldToChargeTriggerState();
        this.trigger = new HoldToChargeTrigger(state);
        this.firing = new ChargedFiringMechanism(state);
    }

    @Override
    public EndlessMagazineHandler getMagazineHandler() {
        return this.magazine;
    }

    @Override
    public HoldToChargeTrigger getTrigger() {
        return this.trigger;
    }

    @Override
    public ChargedFiringMechanism getFiringMechanism() {
        return this.firing;
    }
}
