package fi.jakojaannos.roguelite.game.view.state;

import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.game.view.systems.MainMenuRenderingSystem;
import lombok.val;

import java.nio.file.Path;

public class MainMenuGameStateRenderer extends GameStateRenderer {
    public MainMenuGameStateRenderer(final Path assetRoot, final LWJGLCamera camera) {
        super(createDispatcher(assetRoot, camera));
    }

    private static SystemDispatcher createDispatcher(
            final Path assetRoot,
            final LWJGLCamera camera
    ) {
        val builder = SystemDispatcher.builder()
                                      .withSystem(new MainMenuRenderingSystem());

        // FIXME: Make rendering systems use groups so that no hard dependencies are required.
        //  registering the debug rendering systems fails as they depend on other systems not present.
        //if (DebugConfig.debugModeEnabled) {
        //    builder.withSystem(new EntityTransformRenderingSystem(assetRoot, camera));
        //}

        return builder.build();
    }
}
