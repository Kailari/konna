package fi.jakojaannos.roguelite.game.view.state;

import fi.jakojaannos.roguelite.engine.content.AssetManager;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ui.ProportionValue;
import fi.jakojaannos.roguelite.engine.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.text.Font;
import fi.jakojaannos.roguelite.game.view.systems.UserInterfaceRenderingSystem;
import lombok.val;

import java.nio.file.Path;

public class MainMenuGameStateRenderer extends GameStateRenderer {
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
}
