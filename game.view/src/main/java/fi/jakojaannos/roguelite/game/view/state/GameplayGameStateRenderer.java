package fi.jakojaannos.roguelite.game.view.state;

import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.LWJGLSpriteBatch;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.LWJGLFont;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.LWJGLTextRenderer;
import fi.jakojaannos.roguelite.engine.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.ui.builder.UILabelBuilder;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.view.text.Font;
import fi.jakojaannos.roguelite.game.DebugConfig;
import fi.jakojaannos.roguelite.game.view.systems.*;
import fi.jakojaannos.roguelite.game.view.systems.debug.EntityCollisionBoundsRenderingSystem;
import fi.jakojaannos.roguelite.game.view.systems.debug.EntityTransformRenderingSystem;
import lombok.val;

import java.nio.file.Path;

import static fi.jakojaannos.roguelite.engine.ui.ProportionValue.absolute;
import static fi.jakojaannos.roguelite.engine.ui.ProportionValue.percentOf;

public class GameplayGameStateRenderer extends GameStateRenderer {
    public GameplayGameStateRenderer(
            final Path assetRoot,
            final LWJGLCamera camera,
            final Viewport viewport,
            final SpriteRegistry spriteRegistry,
            final LWJGLTextRenderer textRenderer
    ) {
        super(createDispatcher(assetRoot,
                               createUserInterface(viewport, new LWJGLFont(assetRoot, 1.0f, 1.0f)),
                               camera,
                               viewport,
                               spriteRegistry,
                               textRenderer));
    }

    private static SystemDispatcher createDispatcher(
            final Path assetRoot,
            final UserInterface userInterface,
            final LWJGLCamera camera,
            final Viewport viewport,
            final SpriteRegistry spriteRegistry,
            final LWJGLTextRenderer textRenderer
    ) {
        val font = new LWJGLFont(assetRoot, 1.0f, 1.0f);
        val builder = SystemDispatcher.builder()
                                      .withSystem(new LevelRenderingSystem(assetRoot, camera, spriteRegistry))
                                      .withSystem(new SpriteRenderingSystem(assetRoot, camera, spriteRegistry))
                                      .withSystem(new UserInterfaceRenderingSystem(assetRoot,
                                                                                   camera,
                                                                                   spriteRegistry,
                                                                                   new LWJGLSpriteBatch(assetRoot, "sprite"),
                                                                                   textRenderer,
                                                                                   userInterface))
                                      .withSystem(new UpdateHUDSystem(userInterface))
                                      .withSystem(new RenderGameOverSystem(textRenderer, camera, viewport, font))
                                      .withSystem(new HealthBarRenderingSystem(assetRoot, camera));

        if (DebugConfig.debugModeEnabled) {
            builder.withSystem(new EntityCollisionBoundsRenderingSystem(assetRoot, camera));
            builder.withSystem(new EntityTransformRenderingSystem(assetRoot, camera));
        }

        return builder.build();
    }

    private static UserInterface createUserInterface(final Viewport viewport, final Font font) {
        return UserInterface.builder(viewport, font)
                            .element("time-played-timer",
                                     UIElementType.LABEL,
                                     GameplayGameStateRenderer::buildTimePlayedTimer)
                            .build();
    }

    private static void buildTimePlayedTimer(final UILabelBuilder builder) {
        builder.anchorX(percentOf().parentWidth(0.5))
               .left(percentOf().ownWidth(-0.5))
               .top(absolute(5))
               .fontSize(48)
               .text("12:34:56");
    }
}
