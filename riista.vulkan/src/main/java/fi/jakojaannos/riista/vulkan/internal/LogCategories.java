package fi.jakojaannos.riista.vulkan.internal;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import fi.jakojaannos.riista.utilities.logging.EnumMarker;

public enum LogCategories implements EnumMarker {
    MESH_LOADING;

    private final Marker wrapped;

    @Override
    public Marker getWrapped() {
        return this.wrapped;
    }

    LogCategories() {
        this.wrapped = MarkerFactory.getMarker(name());
    }
}
