package fi.jakojaannos.roguelite.game.view.state;

import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ui.ProportionValue;
import fi.jakojaannos.roguelite.engine.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.content.FontRegistry;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;
import fi.jakojaannos.roguelite.game.view.systems.UserInterfaceRenderingSystem;
import lombok.val;

import java.nio.file.Path;

public class MainMenuGameStateRenderer extends GameStateRenderer {
    public MainMenuGameStateRenderer(
            final Path assetRoot,
            final Camera camera,
            final TextRenderer textRenderer,
            final SpriteRegistry spriteRegistry,
            final FontRegistry fontRegistry,
            final RenderingBackend backend
    ) {
        super(createDispatcher(assetRoot, createUserInterface(fontRegistry, camera.getViewport()), textRenderer, camera, spriteRegistry, fontRegistry, backend));
    }

    private static UserInterface createUserInterface(
            final FontRegistry fontRegistry,
            final UserInterface.ViewportSizeProvider viewportSizeProvider
    ) {
        val font = fontRegistry.getByAssetName("fonts/VCR_OSD_MONO.ttf");
        val width = 600;
        val height = 100;
        val borderSize = 25;
        return UserInterface
                .builder(viewportSizeProvider, font)
                .element("play_button",
                         UIElementType.PANEL,
                         builder -> builder.anchorX(ProportionValue.percentOf().parentWidth(0.5))
                                           .left(ProportionValue.percentOf().ownWidth(-0.5))
                                           .top(ProportionValue.percentOf().parentHeight(0.3))
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
                .element("title_label",
                         UIElementType.LABEL,
                         builder -> builder.anchorX(ProportionValue.percentOf().parentWidth(0.5))
                                           .anchorY(ProportionValue.percentOf().parentHeight(0.25))
                                           .left(ProportionValue.percentOf().ownWidth(-0.5))
                                           .top(ProportionValue.percentOf().ownHeight(-1.0))
                                           .text("Konna")
                                           .fontSize(48))
                .build();
    }

    private static SystemDispatcher createDispatcher(
            final Path assetRoot,
            final UserInterface userInterface,
            final TextRenderer textRenderer,
            final Camera camera,
            final SpriteRegistry spriteRegistry,
            final FontRegistry fontRegistry,
            final RenderingBackend backend
    ) {
        val builder = SystemDispatcher.builder()
                                      .withSystem(new UserInterfaceRenderingSystem(camera,
                                                                                   fontRegistry,
                                                                                   spriteRegistry,
                                                                                   backend.createSpriteBatch(assetRoot, "sprite"),
                                                                                   textRenderer,
                                                                                   userInterface));

        // FIXME: Make rendering systems use groups so that no hard dependencies are required.
        //  registering the debug rendering systems fails as they depend on other systems not present.
        //if (DebugConfig.debugModeEnabled) {
        //    builder.withSystem(new EntityTransformRenderingSystem(assetRoot, camera));
        //}

        return builder.build();
    }
}
