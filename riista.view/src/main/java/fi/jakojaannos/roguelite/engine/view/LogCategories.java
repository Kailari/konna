package fi.jakojaannos.roguelite.engine.view;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import fi.jakojaannos.roguelite.engine.utilities.logging.EnumMarker;

public enum LogCategories implements EnumMarker {
    ASSET_LOADING;

    private final Marker wrapped;

    public Marker getWrapped() {
        return this.wrapped;
    }

    LogCategories() {
        this.wrapped = MarkerFactory.getMarker(name());
    }
}
