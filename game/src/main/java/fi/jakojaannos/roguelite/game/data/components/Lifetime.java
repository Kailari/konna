package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.riista.utilities.TimeManager;

public class Lifetime {
    public long timestamp;
    public long duration;

    public static Lifetime ticks(final long timestamp, final long duration) {
        return new Lifetime(timestamp, duration);
    }

    public static Lifetime seconds(final TimeManager timeManager, final long duration) {
        return new Lifetime(timeManager.getCurrentGameTime(), timeManager.convertToTicks(duration));
    }

    private Lifetime(final long timestamp, final long duration) {
        this.timestamp = timestamp;
        this.duration = duration;
    }
}
