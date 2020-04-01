package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.ecs.ProvidedResource;

public class Weapons implements ProvidedResource {
    public final Weapon unarmed = new NoWeapon();
    public final Weapon simpleWeapon = new SimpleWeapon();
}
