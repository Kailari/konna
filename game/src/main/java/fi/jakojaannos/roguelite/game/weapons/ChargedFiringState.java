package fi.jakojaannos.roguelite.game.weapons;

public interface ChargedFiringState {
    long getLastAttackTimestamp();

    void setLastAttackTimestamp(long timestamp);

    void setHasFired(boolean hasFired);

    long getChargeTime();
}
