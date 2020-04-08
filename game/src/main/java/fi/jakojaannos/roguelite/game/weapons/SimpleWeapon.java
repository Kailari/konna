package fi.jakojaannos.roguelite.game.weapons;

public class SimpleWeapon<MS, TS, FS>
        implements Weapon<MS, TS, FS> {

    private final MagazineHandler<MS> magazine;
    private final TriggerMechanism<TS> trigger;
    private final FiringMechanism<FS> firing;

    public SimpleWeapon(
            final MagazineHandler<MS> magazine,
            final TriggerMechanism<TS> trigger,
            final FiringMechanism<FS> firing
    ) {
        this.magazine = magazine;
        this.trigger = trigger;
        this.firing = firing;
    }

    @Override
    public MagazineHandler<MS> getMagazineHandler() {
        return this.magazine;
    }

    @Override
    public TriggerMechanism<TS> getTrigger() {
        return this.trigger;
    }

    @Override
    public FiringMechanism<FS> getFiringMechanism() {
        return this.firing;
    }

    public static SimpleWeapon<
            EndlessMagazineHandler.EndlessMagazineState,
            AutomaticTriggerState,
            ProjectileFiringState>
    createBasicWeapon() {
        return new SimpleWeapon<>(
                new EndlessMagazineHandler(),
                new AutomaticTriggerMechanism(),
                new ProjectileFiringMechanism());
    }

    public static SimpleWeapon<
            ShotgunMagazineHandler.ShotgunMagazineState,
            SingleShotTrigger.SingleShotTriggerState,
            ShotgunFiringMechanism.ShotgunFiringState>
    createShotgunWeapon() {
        return new SimpleWeapon<>(
                new ShotgunMagazineHandler(),
                new SingleShotTrigger(),
                new ShotgunFiringMechanism());
    }
}
