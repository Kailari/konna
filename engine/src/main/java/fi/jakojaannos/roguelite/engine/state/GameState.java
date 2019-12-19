package fi.jakojaannos.roguelite.engine.state;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import lombok.Getter;

public class GameState implements WorldProvider {
    @Getter private final World world;

    public GameState(final World world, final TimeManager timeManager) {
        this.world = world;
        this.world.createResource(Time.class, new Time(timeManager));
    }
}
