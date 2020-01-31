package fi.jakojaannos.roguelite.game.view.state;

import java.nio.file.Path;

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
import fi.jakojaannos.roguelite.game.view.systems.NetworkHUDSystem;
import fi.jakojaannos.roguelite.game.view.systems.RenderSystemGroups;
import fi.jakojaannos.roguelite.game.view.systems.UserInterfaceRenderingSystem;
import fi.jakojaannos.roguelite.game.view.systems.debug.EntityCollisionBoundsRenderingSystem;
import fi.jakojaannos.roguelite.game.view.systems.debug.EntityTransformRenderingSystem;

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
        final var fontRegistry = assetManager.getAssetRegistry(Font.class);
        final var spriteRegistry = assetManager.getAssetRegistry(Sprite.class);

        final var textRenderer = backend.getTextRenderer();

        final var builder =
                SystemDispatcher.builder()
                                .withGroups(RenderSystemGroups.values())
                                .addGroupDependencies(RenderSystemGroups.DEBUG, RenderSystemGroups.UI,
                                                      RenderSystemGroups.OVERLAY, RenderSystemGroups.ENTITIES,
                                                      RenderSystemGroups.LEVEL)
                                .addGroupDependencies(RenderSystemGroups.UI, RenderSystemGroups.OVERLAY,
                                                      RenderSystemGroups.ENTITIES, RenderSystemGroups.LEVEL)
                                .addGroupDependencies(RenderSystemGroups.OVERLAY, RenderSystemGroups.ENTITIES,
                                                      RenderSystemGroups.LEVEL)
                                .addGroupDependencies(RenderSystemGroups.ENTITIES, RenderSystemGroups.LEVEL)
                                .withSystem(new NetworkHUDSystem(userInterface))
                                .withSystem(new UserInterfaceRenderingSystem(assetRoot,
                                                                             camera,
                                                                             fontRegistry,
                                                                             spriteRegistry,
                                                                             textRenderer,
                                                                             userInterface,
                                                                             backend));

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
        final var fontRegistry = assetManager.getAssetRegistry(Font.class);

        final var font = fontRegistry.getByAssetName("fonts/VCR_OSD_MONO.ttf");
        final var width = 600;
        final var height = 100;
        final var borderSize = 25;
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
                                           .top(ProportionValue.percentOf().ownHeight(2.2))
                                           .width(ProportionValue.absolute(width))
                                           .height(ProportionValue.absolute(height))
                                           .borderSize(borderSize)
                                           .sprite("sprites/ui/ui")
                                           .child("quit_button_label",
                                                  UIElementType.LABEL,
                                                  labelBuilder -> labelBuilder
                                                          .anchorX(ProportionValue.percentOf().parentWidth(0.5))
                                                          .anchorY(ProportionValue.percentOf().parentHeight(0.5))
                                                          .left(ProportionValue.percentOf().ownWidth(-0.5))
                                                          .top(ProportionValue.percentOf().ownHeight(-0.5))
                                                          .text("Quit")
                                                          .fontSize(24)))
                .element("connect_button",
                         UIElementType.PANEL,
                         builder -> builder.anchorX(ProportionValue.percentOf().parentWidth(0.5))
                                           .anchorY(ProportionValue.percentOf().parentHeight(0.3))
                                           .left(ProportionValue.percentOf().ownWidth(-0.5))
                                           .top(ProportionValue.percentOf().ownHeight(1.1))
                                           .width(ProportionValue.absolute(width))
                                           .height(ProportionValue.absolute(height))
                                           .borderSize(borderSize)
                                           .sprite("sprites/ui/ui")
                                           .child("connect_button_label",
                                                  UIElementType.LABEL,
                                                  labelBuilder -> labelBuilder
                                                          .anchorX(ProportionValue.percentOf().parentWidth(0.5))
                                                          .anchorY(ProportionValue.percentOf().parentHeight(0.5))
                                                          .left(ProportionValue.percentOf().ownWidth(-0.5))
                                                          .top(ProportionValue.percentOf().ownHeight(-0.5))
                                                          .text("Connect")
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
