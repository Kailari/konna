package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public interface ChargedTriggerState {
    boolean isTriggerReadyToCharge(TimeManager timeManager, WeaponStats stats);

    boolean isCharging();

    void setCharging(boolean isCharging);

    boolean hasFired();

    void setHasFired(boolean hasFired);

    boolean isTriggerReadyToFire();

    void setTriggerReadyToFire(boolean isReady);

    void setChargeStartTimestamp(long timestamp);

    void setChargeEndTimestamp(long timestamp);
}
