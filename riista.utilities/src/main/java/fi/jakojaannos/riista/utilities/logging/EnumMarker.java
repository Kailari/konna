package fi.jakojaannos.riista.utilities.logging;

import org.slf4j.Marker;

import java.util.Iterator;

public interface EnumMarker extends Marker {
    Marker getWrapped();

    @Override
    default String getName() {
        return getWrapped().getName();
    }

    @Override
    default void add(final Marker reference) {
        getWrapped().add(reference);
    }

    @Override
    default boolean remove(final Marker reference) {
        return getWrapped().remove(reference);
    }

    @Override
    default boolean hasChildren() {
        return getWrapped().hasReferences();
    }

    @Override
    default boolean hasReferences() {
        return getWrapped().hasReferences();
    }

    @Override
    default Iterator<Marker> iterator() {
        return getWrapped().iterator();
    }

    @Override
    default boolean contains(final Marker other) {
        return getWrapped().contains(other);
    }

    @Override
    default boolean contains(final String name) {
        return getWrapped().contains(name);
    }
}
