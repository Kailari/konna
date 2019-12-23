package fi.jakojaannos.roguelite.game.view.state;

import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.LWJGLTexture;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.LWJGLTextRenderer;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.game.DebugConfig;
import fi.jakojaannos.roguelite.game.view.systems.*;
import fi.jakojaannos.roguelite.game.view.systems.debug.EntityCollisionBoundsRenderingSystem;
import fi.jakojaannos.roguelite.game.view.systems.debug.EntityTransformRenderingSystem;
import lombok.val;

import java.nio.file.Path;

public class GameplayGameStateRenderer extends GameStateRenderer {
    public GameplayGameStateRenderer(
            final Path assetRoot,
            final LWJGLCamera camera,
            final SpriteRegistry<LWJGLTexture> spriteRegistry,
            final LWJGLTextRenderer textRenderer
    ) {
        super(createDispatcher(assetRoot, camera, spriteRegistry, textRenderer));
    }

    private static SystemDispatcher createDispatcher(
            final Path assetRoot,
            final LWJGLCamera camera,
            final SpriteRegistry<LWJGLTexture> spriteRegistry,
            final LWJGLTextRenderer textRenderer
    ) {
        val builder = SystemDispatcher.builder()
                                      .withSystem(new LevelRenderingSystem(assetRoot, camera, spriteRegistry))
                                      .withSystem(new SpriteRenderingSystem(assetRoot, camera, spriteRegistry))
                                      .withSystem(new RenderHUDSystem(textRenderer))
                                      .withSystem(new RenderGameOverSystem(textRenderer, camera))
                                      .withSystem(new HealthBarRenderingSystem(assetRoot, camera));

        if (DebugConfig.debugModeEnabled) {
            builder.withSystem(new EntityCollisionBoundsRenderingSystem(assetRoot, camera));
            builder.withSystem(new EntityTransformRenderingSystem(assetRoot, camera));
        }

        return builder.build();
    }
}
