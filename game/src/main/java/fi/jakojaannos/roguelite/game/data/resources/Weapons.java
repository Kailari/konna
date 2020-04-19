package fi.jakojaannos.roguelite.game.data.resources;

import org.joml.Vector2d;

import fi.jakojaannos.roguelite.game.data.events.render.GunshotEvent;
import fi.jakojaannos.roguelite.game.weapons.*;

/**
 * Resource for managing weapon instances.
 */
public class Weapons {
    public static ModularWeapon NO_WEAPON = new ModularWeapon();

    public static ModularWeapon PLAYER_AR = new ModularWeapon(
            new ModularWeapon.Module<>(new AutomaticTriggerModule(),
                                       new NoAttributes()),
            new ModularWeapon.Module<>(new ProjectileFiringModule(),
                                       new ProjectileFiringModule.Attributes(new Vector2d(0.0),
                                                                             7,
                                                                             80,
                                                                             2.5,
                                                                             4.0,
                                                                             -1,
                                                                             10.0,
                                                                             0.85,
                                                                             GunshotEvent.Variant.RIFLE)),
            new ModularWeapon.Module<>(new ClipMagazineModule(),
                                       new ClipMagazineModule.Attributes(30, 60)));
    public static ModularWeapon PLAYER_SHOTGUN = new ModularWeapon(
            new ModularWeapon.Module<>(new ShotgunFiringModule(),
                                       new ShotgunFiringModule.Attributes(new Vector2d(0.0),
                                                                          40,
                                                                          50,
                                                                          10.0,
                                                                          1.0,
                                                                          10,
                                                                          7.5,
                                                                          1.25,
                                                                          12)),
            new ModularWeapon.Module<>(new SingleShotTriggerModule(),
                                       new NoAttributes()),
            new ModularWeapon.Module<>(new ShotgunMagazineModule(),
                                       new ShotgunMagazineModule.Attributes(6, 30)));
    public static ModularWeapon PLAYER_MINIGUN_OVERHEAT_1 = new ModularWeapon(
            new ModularWeapon.Module<>(new AutomaticTriggerModule(),
                                       new NoAttributes()),
            new ModularWeapon.Module<>(new ProjectileFiringModule(),
                                       new ProjectileFiringModule.Attributes(new Vector2d(0.0),
                                                                             3,
                                                                             80,
                                                                             8.0,
                                                                             8.0,
                                                                             -1,
                                                                             10.0,
                                                                             0.85,
                                                                             GunshotEvent.Variant.GATLING)),
            new ModularWeapon.Module<>(new ClipMagazineModule(),
                                       new ClipMagazineModule.Attributes(450, 180)),
            new ModularWeapon.Module<>(new OverheatModule(),
                                       new OverheatModule.Attributes(4.5, 100, 1.0, 200)));
    public static ModularWeapon PLAYER_MINIGUN_OVERHEAT_2 = new ModularWeapon(
            new ModularWeapon.Module<>(new AutomaticTriggerModule(),
                                       new NoAttributes()),
            new ModularWeapon.Module<>(new ProjectileFiringModule(),
                                       new ProjectileFiringModule.Attributes(new Vector2d(0.0),
                                                                             3,
                                                                             80,
                                                                             8.0,
                                                                             8.0,
                                                                             -1,
                                                                             10.0,
                                                                             0.85,
                                                                             GunshotEvent.Variant.GATLING)),
            new ModularWeapon.Module<>(new ClipMagazineModule(),
                                       new ClipMagazineModule.Attributes(450, 180)),
            new ModularWeapon.Module<>(new OverheatFromShotsModule(),
                                       new OverheatFromShotsModule.Attributes(4.5, 100, 1.0, 200)));
    public static ModularWeapon PLAYER_MINIGUN_OVERHEAT_3 = new ModularWeapon(
            new ModularWeapon.Module<>(new AutomaticTriggerModule(),
                                       new NoAttributes()),
            new ModularWeapon.Module<>(new ProjectileFiringModule(),
                                       new ProjectileFiringModule.Attributes(new Vector2d(0.0),
                                                                             3,
                                                                             80,
                                                                             8.0,
                                                                             8.0,
                                                                             -1,
                                                                             10.0,
                                                                             0.85,
                                                                             GunshotEvent.Variant.GATLING)),
            new ModularWeapon.Module<>(new ClipMagazineModule(),
                                       new ClipMagazineModule.Attributes(450, 180)),
            new ModularWeapon.Module<>(new OverheatFromTriggerDownModule(),
                                       new OverheatFromTriggerDownModule.Attributes(4.5, 100, 1.0, 200)));
    public static ModularWeapon TURRET_GATLING = new ModularWeapon(
            new ModularWeapon.Module<>(new AutomaticTriggerModule(),
                                       new NoAttributes()),
            new ModularWeapon.Module<>(new ProjectileFiringModule(),
                                       new ProjectileFiringModule.Attributes(new Vector2d(0.4, -0.5),
                                                                             6,
                                                                             50,
                                                                             5.5,
                                                                             5.0,
                                                                             -1,
                                                                             5.0,
                                                                             0.95,
                                                                             GunshotEvent.Variant.GATLING)));
    public static ModularWeapon SLIME_MELEE = new ModularWeapon(
            new ModularWeapon.Module<>(new AutomaticTriggerModule(),
                                       new NoAttributes()),
            new ModularWeapon.Module<>(new ProjectileFiringModule(),
                                       new ProjectileFiringModule.Attributes(new Vector2d(0.0),
                                                                             20,
                                                                             10,
                                                                             2.0,
                                                                             0.0,
                                                                             15,
                                                                             0.0,
                                                                             1.0,
                                                                             GunshotEvent.Variant.MELEE)));
    public static ModularWeapon FOLLOWER_MELEE = new ModularWeapon(
            new ModularWeapon.Module<>(new AutomaticTriggerModule(),
                                       new NoAttributes()),
            new ModularWeapon.Module<>(new ProjectileFiringModule(),
                                       new ProjectileFiringModule.Attributes(new Vector2d(0.0),
                                                                             20,
                                                                             10,
                                                                             2.0,
                                                                             0.0,
                                                                             10,
                                                                             0.0,
                                                                             1.0,
                                                                             GunshotEvent.Variant.MELEE)));

}
