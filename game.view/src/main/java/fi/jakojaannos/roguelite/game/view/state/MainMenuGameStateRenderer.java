package fi.jakojaannos.roguelite.game.view.state;

import fi.jakojaannos.roguelite.engine.content.AssetManager;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.rendering.text.Font;
import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.game.DebugConfig;
import fi.jakojaannos.roguelite.game.view.systems.RenderSystemGroups;
import fi.jakojaannos.roguelite.game.view.systems.UserInterfaceRenderingSystem;
import fi.jakojaannos.roguelite.game.view.systems.debug.EntityCollisionBoundsRenderingSystem;
import fi.jakojaannos.roguelite.game.view.systems.debug.EntityTransformRenderingSystem;
import lombok.val;

import java.nio.file.Path;

public class MainMenuGameStateRenderer extends GameStateRenderer {

    public static final String TITLE_LABEL_NAME = "title_label";

    public MainMenuGameStateRenderer(
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

        val builder = SystemDispatcher.builder()
                                      .withGroups(RenderSystemGroups.values())
                                      .addGroupDependencies(RenderSystemGroups.DEBUG, RenderSystemGroups.UI, RenderSystemGroups.OVERLAY, RenderSystemGroups.ENTITIES, RenderSystemGroups.LEVEL)
                                      .addGroupDependencies(RenderSystemGroups.UI, RenderSystemGroups.OVERLAY, RenderSystemGroups.ENTITIES, RenderSystemGroups.LEVEL)
                                      .addGroupDependencies(RenderSystemGroups.OVERLAY, RenderSystemGroups.ENTITIES, RenderSystemGroups.LEVEL)
                                      .addGroupDependencies(RenderSystemGroups.ENTITIES, RenderSystemGroups.LEVEL)
                                      .withSystem(new UserInterfaceRenderingSystem(camera,
                                                                                   fontRegistry,
                                                                                   spriteRegistry,
                                                                                   backend.createSpriteBatch(assetRoot, "sprite"),
                                                                                   textRenderer,
                                                                                   userInterface));

        if (DebugConfig.debugModeEnabled) {
            builder.withSystem(new EntityTransformRenderingSystem(assetRoot, camera, backend));
            builder.withSystem(new EntityCollisionBoundsRenderingSystem(assetRoot, camera, backend));
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
        val width = 600;
        val height = 100;
        val borderSize = 25;
        return UserInterface
                .builder(camera.getViewport(), font)
                .element("play_button",
                         UIElementType.PANEL,
                         builder -> builder.anchorX(ProportionValue.percentOf().parentWidth(0.5))
                                           .anchorY(ProportionValue.percentOf().parentHeight(0.3))
                                           .left(ProportionValue.percentOf().ownWidth(-0.5))
                                           .top(ProportionValue.absolute(0))
                                           .width(ProportionValue.absolute(width))
                                           .height(ProportionValue.absolute(height))
                                           .borderSize(borderSize)
                                           .sprite("sprites/ui/ui")
                                           // TODO: panelAnimationName
                                           .child("play_button_label",
                                                  UIElementType.LABEL,
                                                  labelBuilder -> labelBuilder
                                                          .anchorX(ProportionValue.percentOf().parentWidth(0.5))
                                                          .anchorY(ProportionValue.percentOf().parentHeight(0.5))
                                                          .left(ProportionValue.percentOf().ownWidth(-0.5))
                                                          .top(ProportionValue.percentOf().ownHeight(-0.5))
                                                          .text("Play")
                                                          .fontSize(24)))
                .element("quit_button",
                         UIElementType.PANEL,
                         builder -> builder.anchorX(ProportionValue.percentOf().parentWidth(0.5))
                                           .anchorY(ProportionValue.percentOf().parentHeight(0.3))
                                           .left(ProportionValue.percentOf().ownWidth(-0.5))
                                           .top(ProportionValue.percentOf().ownHeight(1.1))
                                           .width(ProportionValue.absolute(width))
                                           .height(ProportionValue.absolute(height))
                                           .borderSize(borderSize)
                                           .sprite("sprites/ui/ui")
                                           // TODO: panelAnimationName
                                           .child("quit_button_label",
                                                  UIElementType.LABEL,
                                                  labelBuilder -> labelBuilder
                                                          .anchorX(ProportionValue.percentOf().parentWidth(0.5))
                                                          .anchorY(ProportionValue.percentOf().parentHeight(0.5))
                                                          .left(ProportionValue.percentOf().ownWidth(-0.5))
                                                          .top(ProportionValue.percentOf().ownHeight(-0.5))
                                                          .text("Quit")
                                                          .fontSize(24)))
                .element(TITLE_LABEL_NAME,
                         UIElementType.LABEL,
                         builder -> builder.anchorX(ProportionValue.percentOf().parentWidth(0.5))
                                           .anchorY(ProportionValue.percentOf().parentHeight(0.25))
                                           .left(ProportionValue.percentOf().ownWidth(-0.5))
                                           .top(ProportionValue.percentOf().ownHeight(-1.0))
                                           .text("Konna")
                                           .fontSize(48))
                .build();
    }
}
