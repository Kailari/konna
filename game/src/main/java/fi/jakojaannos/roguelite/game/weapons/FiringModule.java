package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

public interface FiringModule {
    boolean isReadyToFire(Weapon weapon, TimeManager timeManager);
}
