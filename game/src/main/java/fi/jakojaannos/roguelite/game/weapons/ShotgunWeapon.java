package fi.jakojaannos.roguelite.game.weapons;

public class ShotgunWeapon implements Weapon<
        ShotgunMagazineHandler.ShotgunMagazineState,
        SingleShotTrigger.SingleShotTriggerState,
        ShotgunFiringMechanism.ShotgunFiringState> {

    private final ShotgunMagazineHandler magazine;
    private final SingleShotTrigger trigger;
    private final ShotgunFiringMechanism firing;

    public ShotgunWeapon() {
        this.magazine = new ShotgunMagazineHandler();
        this.trigger = new SingleShotTrigger();
        this.firing = new ShotgunFiringMechanism();
    }

    @Override
    public ShotgunMagazineHandler getMagazineHandler() {
        return this.magazine;
    }

    @Override
    public SingleShotTrigger getTrigger() {
        return this.trigger;
    }

    @Override
    public ShotgunFiringMechanism getFiringMechanism() {
        return this.firing;
    }
}
