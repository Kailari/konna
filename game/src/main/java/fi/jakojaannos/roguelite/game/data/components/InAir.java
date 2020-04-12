package fi.jakojaannos.roguelite.game.data.components;

public class InAir {
    public long flightStartTimeStamp;
    public long flightDuration;

    public InAir(final long flightStartTimeStamp, final long flightDuration) {
        this.flightStartTimeStamp = flightStartTimeStamp;
        this.flightDuration = flightDuration;
    }
}
