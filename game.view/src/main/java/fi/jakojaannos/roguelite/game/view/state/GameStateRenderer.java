package fi.jakojaannos.roguelite.game.view.state;

import lombok.Getter;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.engine.content.AssetManager;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public abstract class GameStateRenderer implements AutoCloseable {
    private final SystemDispatcher rendererDispatcher;
    @Getter private final UserInterface userInterface;

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
        this.rendererDispatcher.dispatch(world);
    }

    @Override
    public void close() throws Exception {
        this.rendererDispatcher.close();
    }
}
