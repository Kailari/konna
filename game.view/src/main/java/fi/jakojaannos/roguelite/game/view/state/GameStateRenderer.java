package fi.jakojaannos.roguelite.game.view.state;

import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class GameStateRenderer implements AutoCloseable {
    private final SystemDispatcher rendererDispatcher;

    public void render(final World world) {
        this.rendererDispatcher.dispatch(world);
    }

    @Override
    public void close() throws Exception {
        this.rendererDispatcher.close();
    }
}
