package fi.jakojaannos.roguelite.game.view.state;

import fi.jakojaannos.roguelite.engine.content.AssetManager;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;

import java.nio.file.Path;

public abstract class GameStateRenderer implements AutoCloseable {
    private final SystemDispatcher rendererDispatcher;
    private final UserInterface userInterface;

    protected GameStateRenderer(
            final Path assetRoot,
            final Camera camera,
            final AssetManager assetManager,
            final RenderingBackend backend
    ) {
        this.userInterface = createUserInterface(camera, assetManager);
        this.rendererDispatcher = createRenderDispatcher(this.userInterface, assetRoot, camera, assetManager, backend);
    }

    protected abstract SystemDispatcher createRenderDispatcher(
            final UserInterface userInterface,
            final Path assetRoot,
            final Camera camera,
            final AssetManager assetManager,
            final RenderingBackend backend
    );

    protected abstract UserInterface createUserInterface(
            final Camera camera,
            final AssetManager assetManager
    );

    public void render(final World world) {
        this.rendererDispatcher.dispatch(world);
    }

    @Override
    public void close() throws Exception {
        this.rendererDispatcher.close();
    }
}
