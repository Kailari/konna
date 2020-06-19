package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.riista.utilities.TimeManager;

public interface FiringModule {
    boolean isReadyToFire(Weapon weapon, TimeManager timeManager);
}
