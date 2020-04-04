package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

/* TODO: could change class to behave such that when user tried to fire, reloading is stopped at the end of the next
    "reload-cycle". Example:
    Reload speed = 4 ticks, magazine is empty
    Tick #-1:                               ammoLeft=1  canFire=true    isReloading=false
    Tick #0: user starts reloading          ammoLeft=1  canFire=false   isReloading=true
    Tick #4: first reload cycle finishes    ammoLeft=2  canFire=false   isReloading=true
    Tick #6: user tries to shoot            ammoLeft=2  canFire=false   isReloading=false
    Tick #8: second cycle finishes          ammoLeft=3  canFire=true    isReloading=false
 */

/**
 * A magazine that reloads one shot at a time, but will reload clip to full with one press of the reload button
 * (provided reloading is not interrupted). Firing (or trying to fire) the weapon causes reloading to stop.
 */
public class ShotgunMagazineHandler implements Weapon.MagazineHandler<ShotgunMagazineHandler.ShotgunMagazineState> {
    @Override
    public ShotgunMagazineState createState(final WeaponStats stats) {
        final var mag = new ShotgunMagazineState();
        mag.ammoLeft = stats.magazineCapacity;
        return mag;
    }

    @Override
    public boolean canFire(
            final ShotgunMagazineState state,
            final WeaponStats stats,
            final TimeManager timeManager
    ) {
        // 2. Entity wants to shoot. If he was reloading, it gets interrupted (and the magazine is updated)
        stopReloadAndUpdateMagazine(state, stats, timeManager);
        // 4. If he reloaded for long enough, he now has bullets in his magazine
        return hasAmmoLeft(state);
    }

    @Override
    public void expendAmmo(
            final ShotgunMagazineState state,
            final WeaponStats stats
    ) {
        state.ammoLeft--;
        if (state.ammoLeft < 0) {
            state.ammoLeft = 0;
        }
    }

    @Override
    public void reload(
            final ShotgunMagazineState state,
            final WeaponStats stats,
            final TimeManager timeManager
    ) {
        if (state.isReloading) return;
        if (state.ammoLeft == stats.magazineCapacity) return;

        // 1. Entity wants to reload, we start reloading the clip
        state.reloadStartTimeStamp = timeManager.getCurrentGameTime();
        state.isReloading = true;
    }

    private void stopReloadAndUpdateMagazine(
            final ShotgunMagazineState state,
            final WeaponStats stats,
            final TimeManager timeManager
    ) {
        if (!state.isReloading) return;
        state.isReloading = false;

        if (stats.reloadTimeInTicks <= 0) {
            state.ammoLeft = stats.magazineCapacity;
            return;
        }

        // 3. Calculate ammo as a function of time
        final long timeUsedReloading = timeManager.getCurrentGameTime() - state.reloadStartTimeStamp;
        final long shellsReloaded = timeUsedReloading / stats.reloadTimeInTicks;

        if (state.ammoLeft + shellsReloaded > stats.magazineCapacity) {
            state.ammoLeft = stats.magazineCapacity;
        } else {
            state.ammoLeft += shellsReloaded;
        }
    }

    private boolean hasAmmoLeft(final ShotgunMagazineState state) {
        return state.ammoLeft > 0;
    }

    public static class ShotgunMagazineState {
        public boolean isReloading;
        public int ammoLeft;
        public long reloadStartTimeStamp = -1000;
    }
}
