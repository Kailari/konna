package fi.jakojaannos.roguelite.game;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import fi.jakojaannos.roguelite.engine.utilities.logging.EnumMarker;

public enum LogCategories implements EnumMarker {
    HEALTH,
    NET_CONNECTION;

    private final Marker wrapped;

    @Override
    public Marker getWrapped() {
        return this.wrapped;
    }

    LogCategories() {
        this.wrapped = MarkerFactory.getMarker(name());
    }
}
