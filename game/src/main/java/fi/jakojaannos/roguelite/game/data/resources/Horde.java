package fi.jakojaannos.roguelite.game.data.resources;

public class Horde {
    public long startTimestamp = -1000;
    public long endTimestamp;
    public long hordeIndex;
    public long changeTimestamp;
    public Status status = Status.INACTIVE;

    public enum Status {
        ACTIVE,
        ENDING,
        INACTIVE
    }
}
