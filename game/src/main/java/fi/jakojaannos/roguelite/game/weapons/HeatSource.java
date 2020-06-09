package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.riista.utilities.TimeManager;

public interface HeatSource {
    /**
     * Gets change in heat value since last query. Returns positive values for heat gain and negative for cooling.
     * <p>
     * Implementors should make sure to reset any heat related values on query, so that the next query will return
     * change relative to last query, rather than change since initialisation.
     *
     * @param weapon      weapon instance
     * @param timeManager timeManager
     *
     * @return change in heat since last query
     */
    double getHeatDeltaSinceLastQuery(
            Weapon weapon,
            final TimeManager timeManager
    );
}
