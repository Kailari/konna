package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class EndlessMagazineHandler implements Weapon.MagazineHandler<EndlessMagazineHandler.EndlessMagazineState> {

    @Override
    public EndlessMagazineState createState(final WeaponStats stats) {
        return new EndlessMagazineState();
    }

    @Override
    public boolean canFire(
            final EndlessMagazineState endlessState,
            final WeaponStats stats,
            final TimeManager timeManager
    ) {
        return true;
    }

    @Override
    public void expendAmmo(
            final EndlessMagazineState endlessState,
            final WeaponStats stats
    ) {
    }

    @Override
    public void reload(
            final EndlessMagazineState endlessState,
            final WeaponStats stats,
            final TimeManager timeManager
    ) {
    }

    public static class EndlessMagazineState {
    }
}
