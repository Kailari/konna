package fi.jakojaannos.roguelite.game.view.state;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.engine.content.AssetManager;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

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

    public UserInterface getUserInterface() {
        return this.userInterface;
    }

    protected abstract SystemDispatcher createRenderDispatcher(
            UserInterface userInterface,
            Path assetRoot,
            Camera camera,
            AssetManager assetManager,
            RenderingBackend backend
    );

    protected abstract UserInterface createUserInterface(
            Camera camera,
            AssetManager assetManager
    );

    public void render(final World world) {
        this.rendererDispatcher.tick(world);
    }

    @Override
    public void close() throws Exception {
        this.rendererDispatcher.close();
    }
}
