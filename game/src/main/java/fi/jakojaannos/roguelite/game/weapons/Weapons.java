package fi.jakojaannos.roguelite.game.weapons;

public class Weapons {
    public static ModularWeapon NO_WEAPON = new ModularWeapon();

    public static ModularWeapon BASIC_WEAPON = new ModularWeapon(
            new AutomaticTriggerModule(),
            new ProjectileFiringModule());
}
