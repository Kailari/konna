package fi.jakojaannos.roguelite.engine.ecs;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import fi.jakojaannos.riista.utilities.logging.EnumMarker;

public enum LogCategories implements EnumMarker {
    ENTITY_LIFECYCLE,
    SYSTEM_DATA_DUMP,
    DISPATCHER,
    DISPATCHER_TICK,
    DISPATCHER_GROUP,
    DISPATCHER_SYSTEM;

    static {
        // Make dispatcher child markers rely on the master dispatcher marker
        DISPATCHER_TICK.add(DISPATCHER);
        DISPATCHER_GROUP.add(DISPATCHER);
        DISPATCHER_SYSTEM.add(DISPATCHER);
    }

    private final Marker wrapped;

    @Override
    public Marker getWrapped() {
        return this.wrapped;
    }

    LogCategories() {
        this.wrapped = MarkerFactory.getMarker(name());
    }
}
