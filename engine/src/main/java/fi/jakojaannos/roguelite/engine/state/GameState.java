package fi.jakojaannos.roguelite.engine.state;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.utilities.UpdateableTimeManager;
import lombok.Getter;

public class GameState implements TimeProvider, WorldProvider, WritableTimeProvider {
    @Getter private final World world;
    private final UpdateableTimeManager time;

    public GameState(final World world, final UpdateableTimeManager time) {
        this.world = world;
        this.time = time;

        this.world.getResource(Time.class).setTimeManager(this.time);
    }

    @Override
    public TimeManager getTime() {
        return this.time;
    }

    @Override
    public void updateTime() {
        this.time.refresh();
    }
}
