package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;

public class InAir implements Component {
    public long flightStartTimeStamp;
    public long flightDuration;

    public InAir(final long flightStartTimeStamp, final long flightDuration) {
        this.flightStartTimeStamp = flightStartTimeStamp;
        this.flightDuration = flightDuration;
    }
}
