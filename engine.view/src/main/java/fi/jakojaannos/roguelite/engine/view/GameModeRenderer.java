package fi.jakojaannos.roguelite.engine.view;

import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public final record GameModeRenderer(
        SystemDispatcher renderDispatcher,
        UserInterface userInterface
) implements AutoCloseable {
    public void render(final World world) {
        this.renderDispatcher.tick(world);
    }

    @Override
    public void close() throws Exception {
        this.renderDispatcher.close();
    }
}
