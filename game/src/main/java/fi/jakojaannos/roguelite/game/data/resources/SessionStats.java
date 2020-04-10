package fi.jakojaannos.roguelite.game.data.resources;

import java.util.HashMap;
import java.util.Map;

import fi.jakojaannos.roguelite.game.data.DamageSource;

public class SessionStats {
    private final Map<Object, Integer> kills = new HashMap<>();
    public long beginTimeStamp;
    public long endTimeStamp;

    public SessionStats(final long timestamp) {
        this.beginTimeStamp = timestamp;
        this.endTimeStamp = timestamp;
    }

    public int getKillsOf(final DamageSource<?> source) {
        return this.kills.getOrDefault(source.getKiller(), 0);
    }

    public void awardKillTo(final DamageSource<?> source) {
        this.kills.compute(source.getKiller(), (key, current) -> (current == null) ? 1 : current + 1);
    }

    public void clearKillsOf(final DamageSource<?> source) {
        this.kills.remove(source.getKiller());
    }

    public void setKillsOf(final DamageSource<?> source, final int amount) {
        this.kills.put(source.getKiller(), amount);
    }
}
