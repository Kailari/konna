package fi.jakojaannos.roguelite.game.view.state;

import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.LWJGLSpriteBatch;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.LWJGLFont;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.LWJGLTextRenderer;
import fi.jakojaannos.roguelite.engine.ui.ProportionValue;
import fi.jakojaannos.roguelite.engine.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.game.view.systems.UserInterfaceRenderingSystem;
import lombok.val;

import java.nio.file.Path;

public class MainMenuGameStateRenderer extends GameStateRenderer {
    public MainMenuGameStateRenderer(
            final Path assetRoot,
            final LWJGLCamera camera,
            final LWJGLTextRenderer textRenderer,
            final SpriteRegistry spriteRegistry
    ) {
        super(createDispatcher(assetRoot, createUserInterface(assetRoot, camera.getViewport()), textRenderer, camera, spriteRegistry));
    }

    private static UserInterface createUserInterface(
            final Path assetRoot,
            final UserInterface.ViewportSizeProvider viewportSizeProvider
    ) {
        val font = new LWJGLFont(assetRoot, 1.0f, 1.0f);
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
            final LWJGLTextRenderer textRenderer,
            final LWJGLCamera camera,
            final SpriteRegistry spriteRegistry
    ) {
        val builder = SystemDispatcher.builder()
                                      .withSystem(new UserInterfaceRenderingSystem(assetRoot,
                                                                                   camera,
                                                                                   spriteRegistry,
                                                                                   new LWJGLSpriteBatch(assetRoot, "sprite"),
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
