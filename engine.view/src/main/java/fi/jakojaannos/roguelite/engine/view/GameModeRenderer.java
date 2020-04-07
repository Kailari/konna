package fi.jakojaannos.roguelite.engine.view;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.content.AssetManager;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public abstract class GameModeRenderer<TGameMode extends GameMode> implements AutoCloseable {
    private final SystemDispatcher rendererDispatcher;
    private final UserInterface userInterface;

    public UserInterface getUserInterface() {
        return this.userInterface;
    }

    protected GameModeRenderer(
            final Events events,
            final TGameMode gameMode,
            final Path assetRoot,
            final Camera camera,
            final AssetManager assetManager,
            final RenderingBackend backend
    ) {
        this.userInterface = createUserInterface(events, gameMode, camera, assetManager);
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
            Events events,
            TGameMode game,
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
