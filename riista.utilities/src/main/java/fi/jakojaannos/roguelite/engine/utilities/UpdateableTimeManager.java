package fi.jakojaannos.roguelite.engine.utilities;

import fi.jakojaannos.riista.utilities.TimeManager;

@Deprecated
public interface UpdateableTimeManager extends TimeManager {
    void refresh();
}
