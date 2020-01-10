package fi.jakojaannos.roguelite.game;

import lombok.experimental.Delegate;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public enum LogCategories implements Marker {
    HEALTH,
    DEATH,
    NET_CONNECTION;

    private final @Delegate Marker wrapped;

    LogCategories() {
        this.wrapped = MarkerFactory.getMarker(name());
    }
}
