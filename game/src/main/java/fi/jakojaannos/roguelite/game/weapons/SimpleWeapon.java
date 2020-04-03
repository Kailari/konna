package fi.jakojaannos.roguelite.game.weapons;

public class SimpleWeapon implements Weapon<
        ClipMagazineHandler,
        AutomaticTriggerMechanism,
        ProjectileFiringMechanism,
        ClipMagazineState,
        AutomaticTriggerState,
        ProjectileFiringState
        > {

    private final ClipMagazineHandler magazine;
    private final AutomaticTriggerMechanism trigger;
    private final ProjectileFiringMechanism firing;

    public SimpleWeapon() {
        this.magazine = new ClipMagazineHandler();
        this.trigger = new AutomaticTriggerMechanism();
        this.firing = new ProjectileFiringMechanism();
    }

    @Override
    public ClipMagazineHandler getMagazineHandler() {
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
