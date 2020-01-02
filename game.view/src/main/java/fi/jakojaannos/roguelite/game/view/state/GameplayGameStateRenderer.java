package fi.jakojaannos.roguelite.game.view.state;

import fi.jakojaannos.roguelite.engine.content.AssetManager;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.ui.builder.UILabelBuilder;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.sprite.Sprite;
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
            final Camera camera,
            final AssetManager assetManager,
            final RenderingBackend backend
    ) {
        super(assetRoot, camera, assetManager, backend);
    }

    @Override
    protected SystemDispatcher createRenderDispatcher(
            final UserInterface userInterface,
            final Path assetRoot,
            final Camera camera,
            final AssetManager assetManager,
            final RenderingBackend backend
    ) {
        val fontRegistry = assetManager.getAssetRegistry(Font.class);
        val spriteRegistry = assetManager.getAssetRegistry(Sprite.class);
        val textRenderer = backend.getTextRenderer();

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
                                      .withSystem(new RenderGameOverSystem(textRenderer, camera, font))
                                      .withSystem(new HealthBarRenderingSystem(assetRoot, camera, backend));

        if (DebugConfig.debugModeEnabled) {
            builder.withSystem(new EntityCollisionBoundsRenderingSystem(assetRoot, camera, backend));
            builder.withSystem(new EntityTransformRenderingSystem(assetRoot, camera, backend));
        }

        return builder.build();
    }

    @Override
    protected UserInterface createUserInterface(
            final Camera camera,
            final AssetManager assetManager
    ) {
        val fontRegistry = assetManager.getAssetRegistry(Font.class);

        val font = fontRegistry.getByAssetName("fonts/VCR_OSD_MONO.ttf");
        return UserInterface.builder(camera.getViewport(), font)
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
