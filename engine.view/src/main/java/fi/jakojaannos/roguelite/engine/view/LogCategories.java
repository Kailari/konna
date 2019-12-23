package fi.jakojaannos.roguelite.engine.view;

import lombok.experimental.Delegate;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public enum LogCategories implements Marker {
    ANIMATION,
    SPRITE_SERIALIZATION,
    UI_BUILDER;

    private @Delegate final Marker wrapped;

    LogCategories() {
        this.wrapped = MarkerFactory.getMarker(name());
    }
}
