package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.event.Events;

public interface GameMode extends AutoCloseable {
    GameState createState(World world);

    void tick(GameState state);
}
