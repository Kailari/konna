package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class ClipMagazineHandler implements Weapon.MagazineHandler<ClipMagazineState> {

    @Override
    public ClipMagazineState createState(final WeaponStats stats) {
        final var clip = new ClipMagazineState();
        clip.ammoLeft = stats.magazineCapacity;
        return clip;
    }

    @Override
    public boolean canFire(
            final ClipMagazineState state,
            final WeaponStats stats,
            final TimeManager timeManager
    ) {
        return hasAmmoLeft(state)
                && !isReloading(state, stats, timeManager);
    }

    private boolean hasAmmoLeft(final ClipMagazineState state) {
        return state.ammoLeft > 0;
    }

    private boolean isReloading(final ClipMagazineState state, final WeaponStats stats, final TimeManager timeManager) {
        return state.reloadStartTimeStamp + stats.reloadTimeInTicks > timeManager.getCurrentGameTime();
    }

    @Override
    public void expendAmmo(final ClipMagazineState state, final WeaponStats stats) {
        state.ammoLeft--;
        if (state.ammoLeft < 0) {
            state.ammoLeft = 0;
        }
    }

    @Override
    public void reload(
            final ClipMagazineState state,
            final WeaponStats stats,
            final TimeManager timeManager
    ) {
        if (isReloading(state, stats, timeManager)) return;
        if (state.ammoLeft == stats.magazineCapacity) return;
        state.ammoLeft = stats.magazineCapacity;
        state.reloadStartTimeStamp = timeManager.getCurrentGameTime();
    }
}
