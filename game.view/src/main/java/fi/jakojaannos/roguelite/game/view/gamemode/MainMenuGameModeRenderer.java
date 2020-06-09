package fi.jakojaannos.roguelite.game.view.gamemode;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.engine.content.AssetManager;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.GameModeRenderer;
import fi.jakojaannos.roguelite.engine.view.RenderDispatcher;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.rendering.text.Font;
import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.game.DebugConfig;
import fi.jakojaannos.roguelite.game.view.systems.NetworkHUDSystem;
import fi.jakojaannos.roguelite.game.view.systems.UserInterfaceRenderingSystem;
import fi.jakojaannos.roguelite.game.view.systems.debug.EntityCollisionBoundsRenderingSystem;
import fi.jakojaannos.roguelite.game.view.systems.debug.EntityTransformRenderingSystem;

import static fi.jakojaannos.roguelite.engine.view.ui.ProportionValue.*;

public final class MainMenuGameModeRenderer {
    public static final String TITLE_LABEL_NAME = "title_label";

    private MainMenuGameModeRenderer() {
    }

    public static GameModeRenderer create(
            final Events events,
            final TimeManager timeManager,
            final Path assetRoot,
            final Camera camera,
            final AssetManager assetManager,
            final RenderingBackend backend
    ) {
        final var userInterface = createUserInterface(events, timeManager, camera, assetManager);
        final var legacyDispatcher = createLegacyDispatcher(userInterface, assetRoot, camera, assetManager, backend);
        final var renderDispatcher = RenderDispatcher.builder().build();
        return new GameModeRenderer(legacyDispatcher, renderDispatcher, userInterface);
    }

    private static SystemDispatcher createLegacyDispatcher(
            final UserInterface userInterface,
            final Path assetRoot,
            final Camera camera,
            final AssetManager assetManager,
            final RenderingBackend backend
    ) {
        final var fontRegistry = assetManager.getAssetRegistry(Font.class);
        final var spriteRegistry = assetManager.getAssetRegistry(Sprite.class);

        final var textRenderer = backend.getTextRenderer();

        final var builder = SystemDispatcher.builder();
        final var ui = builder.group("menu-ui")
                              .withSystem(new NetworkHUDSystem(userInterface))
                              .withSystem(new UserInterfaceRenderingSystem(assetRoot,
                                                                           camera,
                                                                           fontRegistry,
                                                                           spriteRegistry,
                                                                           textRenderer,
                                                                           userInterface,
                                                                           backend))
                              .dependsOn()
                              .buildGroup();

        if (DebugConfig.debugModeEnabled) {
            builder.group("debug")
                   .withSystem(new EntityCollisionBoundsRenderingSystem(assetRoot, camera, backend))
                   .withSystem(new EntityTransformRenderingSystem(assetRoot, camera, backend))
                   .dependsOn(ui)
                   .buildGroup();
        }

        return builder.build();
    }

    private static UserInterface createUserInterface(
            final Events events,
            final TimeManager timeManager,
            final Camera camera,
            final AssetManager assetManager
    ) {
        final var fontRegistry = assetManager.getAssetRegistry(Font.class);

        final var font = fontRegistry.getByAssetName("fonts/VCR_OSD_MONO.ttf");
        final var width = 600;
        final var height = 100;
        final var borderSize = 25;
        return UserInterface
                .builder(events, timeManager, camera.getViewport(), font)
                .element("play_button",
                         builder -> builder.anchorX(percentOf(parentWidth(0.5)))
                                           .anchorY(percentOf(parentHeight(0.3)))
                                           .left(percentOf(ownWidth(-0.5)))
                                           .top(absolute(0))
                                           .width(absolute(width))
                                           .height(absolute(height))
                                           .borderSize(borderSize)
                                           .sprite("sprites/ui/ui")
                                           .child("play_button_label",
                                                  labelBuilder -> labelBuilder
                                                          .anchorX(percentOf(parentWidth(0.5)))
                                                          .anchorY(percentOf(parentHeight(0.5)))
                                                          .left(percentOf(ownWidth(-0.5)))
                                                          .top(percentOf(ownHeight(-0.5)))
                                                          .text("Play")
                                                          .fontSize(24)))
                .element("quit_button",
                         builder -> builder.anchorX(percentOf(parentWidth(0.5)))
                                           .anchorY(percentOf(parentHeight(0.3)))
                                           .left(percentOf(ownWidth(-0.5)))
                                           .top(percentOf(ownHeight(2.2)))
                                           .width(absolute(width))
                                           .height(absolute(height))
                                           .borderSize(borderSize)
                                           .sprite("sprites/ui/ui")
                                           .child("quit_button_label",
                                                  labelBuilder -> labelBuilder
                                                          .anchorX(percentOf(parentWidth(0.5)))
                                                          .anchorY(percentOf(parentHeight(0.5)))
                                                          .left(percentOf(ownWidth(-0.5)))
                                                          .top(percentOf(ownHeight(-0.5)))
                                                          .text("Quit")
                                                          .fontSize(24)))
                .element("connect_button",
                         builder -> builder.anchorX(percentOf(parentWidth(0.5)))
                                           .anchorY(percentOf(parentHeight(0.3)))
                                           .left(percentOf(ownWidth(-0.5)))
                                           .top(percentOf(ownHeight(1.1)))
                                           .width(ProportionValue.absolute(width))
                                           .height(ProportionValue.absolute(height))
                                           .borderSize(borderSize)
                                           .sprite("sprites/ui/ui")
                                           .child("connect_button_label",
                                                  labelBuilder -> labelBuilder
                                                          .anchorX(percentOf(parentWidth(0.5)))
                                                          .anchorY(percentOf(parentHeight(0.5)))
                                                          .left(percentOf(ownWidth(-0.5)))
                                                          .top(percentOf(ownHeight(-0.5)))
                                                          .text("Connect")
                                                          .fontSize(24)))
                .element(TITLE_LABEL_NAME,
                         builder -> builder.anchorX(percentOf(parentWidth(0.5)))
                                           .anchorY(percentOf(parentHeight(0.25)))
                                           .left(percentOf(ownWidth(-0.5)))
                                           .top(percentOf(ownHeight(-1.0)))
                                           .text("Konna")
                                           .fontSize(48))
                .build();
    }
}
