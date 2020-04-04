package fi.jakojaannos.roguelite.game.weapons;

public class SimpleWeapon
        implements Weapon<EndlessMagazineHandler.EndlessMagazineState, AutomaticTriggerState, ProjectileFiringState> {

    private final EndlessMagazineHandler magazine;
    private final AutomaticTriggerMechanism trigger;
    private final ProjectileFiringMechanism firing;

    public SimpleWeapon() {
        this.magazine = new EndlessMagazineHandler();
        this.trigger = new AutomaticTriggerMechanism();
        this.firing = new ProjectileFiringMechanism();
    }

    @Override
    public EndlessMagazineHandler getMagazineHandler() {
        return this.magazine;
    }

    @Override
    public AutomaticTriggerMechanism getTrigger() {
        return this.trigger;
    }

    @Override
    public ProjectileFiringMechanism getFiringMechanism() {
        return this.firing;
    }
}
