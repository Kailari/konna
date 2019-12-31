package fi.jakojaannos.roguelite.game.view.state;

import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.ui.builder.UILabelBuilder;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.text.Font;
import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;
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
            final Camera camera,
            final Viewport viewport,
            final AssetRegistry<Sprite> spriteRegistry,
            final AssetRegistry<Font> fontRegistry,
            final TextRenderer textRenderer,
            final RenderingBackend backend
    ) {
        super(createDispatcher(assetRoot,
                               createUserInterface(viewport, fontRegistry),
                               camera,
                               viewport,
                               spriteRegistry,
                               fontRegistry,
                               textRenderer,
                               backend));
    }

    private static SystemDispatcher createDispatcher(
            final Path assetRoot,
            final UserInterface userInterface,
            final Camera camera,
            final Viewport viewport,
            final AssetRegistry<Sprite> spriteRegistry,
            final AssetRegistry<Font> fontRegistry,
            final TextRenderer textRenderer,
            final RenderingBackend backend
    ) {
        val font = fontRegistry.getByAssetName("fonts/VCR_OSD_MONO.ttf");
        val builder = SystemDispatcher.builder()
                                      .withSystem(new LevelRenderingSystem(assetRoot, camera, spriteRegistry, backend))
                                      .withSystem(new SpriteRenderingSystem(assetRoot, camera, spriteRegistry, backend))
                                      .withSystem(new UserInterfaceRenderingSystem(camera,
                                                                                   fontRegistry,
                                                                                   spriteRegistry,
                                                                                   backend.createSpriteBatch(assetRoot, "sprite"),
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

    private static UserInterface createUserInterface(
            final Viewport viewport,
            final AssetRegistry<Font> fontRegistry
    ) {
        val font = fontRegistry.getByAssetName("fonts/VCR_OSD_MONO.ttf");
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
