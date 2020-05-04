package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

public interface HeatSource {
    /**
     * Get change in heat value since last query. Return positive values for heat gain and negative for heat loss. You
     * must reset values on query.
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
