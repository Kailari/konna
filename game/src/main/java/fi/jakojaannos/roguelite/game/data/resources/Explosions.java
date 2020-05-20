package fi.jakojaannos.roguelite.game.data.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Explosions {
    private final List<RecentExplosion> explosions = Collections.synchronizedList(new ArrayList<>());

    public void addExplosion(final RecentExplosion explosion) {
        this.explosions.add(explosion);
    }

    public List<RecentExplosion> getExplosions(){
        return this.explosions;
    }

    public void clear() {
        this.explosions.clear();
    }
}
