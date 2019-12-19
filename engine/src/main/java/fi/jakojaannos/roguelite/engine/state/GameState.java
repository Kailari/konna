package fi.jakojaannos.roguelite.engine.state;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import lombok.Getter;

public abstract class GameState implements WorldProvider {
    @Getter private final World world;
    private final SystemDispatcher dispatcher;

    public GameState(final World world, final TimeManager timeManager) {
        this.world = world;
        this.world.createResource(Time.class, new Time(timeManager));

        this.dispatcher = createDispatcher();
    }

    protected abstract SystemDispatcher createDispatcher();

    public void tick() {
        this.dispatcher.dispatch(this.world);
        this.world.getEntityManager().applyModifications();
    }
}
