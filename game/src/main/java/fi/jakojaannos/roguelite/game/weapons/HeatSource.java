package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

public interface HeatSource {
    /**
     * Gets change in heat value since last query. Returns positive values for heat gain and negative for cooling.
     *
     * @param weapon      weapon
     * @param timeManager timeManager
     *
     * @return change in heat since last query
     */
    double getHeatDeltaSinceLastQuery(
            Weapon weapon,
            final TimeManager timeManager
    );
}
