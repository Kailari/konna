package fi.jakojaannos.roguelite.game.data.resources;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.game.data.DamageSource;

import java.util.HashMap;
import java.util.Map;

public class Kills implements Resource {
    private final Map<Object, Integer> kills = new HashMap<>();

    public int getKillsOf(final DamageSource source) {
        return this.kills.getOrDefault(source.getKiller(), 0);
    }

    public void awardKillTo(final DamageSource source) {
        this.kills.compute(source.getKiller(), (key, current) -> (current == null) ? 1 : current + 1);
    }

    public void clearKillsOf(final Entity entity) {
        this.kills.remove(entity);
    }
}
