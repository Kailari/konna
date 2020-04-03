package fi.jakojaannos.roguelite.game.weapons;

public class WeaponState<
        MS extends Weapon.WeaponMagazineState,
        TS extends Weapon.WeaponTriggerState,
        FS extends Weapon.WeaponFiringState> {
    private final MS magazine;
    private final TS trigger;
    private final FS firing;

    public WeaponState(final MS mag, final TS trig, final FS fir) {
        this.magazine = mag;
        this.trigger = trig;
        this.firing = fir;
    }

    public MS getMagazine() {
        return this.magazine;
    }

    public TS getTrigger() {
        return this.trigger;
    }

    public FS getFiring() {
        return this.firing;
    }
}
