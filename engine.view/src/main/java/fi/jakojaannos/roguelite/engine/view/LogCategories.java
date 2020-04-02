package fi.jakojaannos.roguelite.engine.view;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Iterator;

public enum LogCategories implements Marker {
    ANIMATION,
    SPRITE_SERIALIZATION,
    UI_BUILDER;

    private final Marker wrapped;

    LogCategories() {
        this.wrapped = MarkerFactory.getMarker(name());
    }

    @Override
    public String getName() {
        return this.wrapped.getName();
    }

    @Override
    public void add(final Marker reference) {
        this.wrapped.add(reference);
    }

    @Override
    public boolean remove(final Marker reference) {
        return this.wrapped.remove(reference);
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public boolean hasReferences() {
        return this.wrapped.hasReferences();
    }

    @Override
    public Iterator<Marker> iterator() {
        return this.wrapped.iterator();
    }

    @Override
    public boolean contains(final Marker other) {
        return this.wrapped.contains(other);
    }

    @Override
    public boolean contains(final String name) {
        return this.wrapped.contains(name);
    }
}
