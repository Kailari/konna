package fi.jakojaannos.roguelite.engine.view.data.components.internal.events;

import fi.jakojaannos.roguelite.engine.ecs.Component;

public class ClickTimestamp implements Component {
    public long timestamp;
    public boolean releasedSince = true;
}
