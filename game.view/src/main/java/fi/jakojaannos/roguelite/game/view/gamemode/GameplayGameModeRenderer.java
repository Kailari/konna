package fi.jakojaannos.roguelite.game.view.gamemode;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.engine.content.AssetManager;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.GameModeRenderer;
import fi.jakojaannos.roguelite.engine.view.RenderDispatcher;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.audio.AudioContext;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.rendering.text.Font;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementBuilder;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.game.DebugConfig;
import fi.jakojaannos.roguelite.game.view.adapters.TurretAdapter;
import fi.jakojaannos.roguelite.game.view.systems.*;
import fi.jakojaannos.roguelite.game.view.systems.audio.BackgroundMusicLoopSystem;
import fi.jakojaannos.roguelite.game.view.systems.audio.HandleAudioEventsSystem;
import fi.jakojaannos.roguelite.game.view.systems.debug.EntityCollisionBoundsRenderingSystem;
import fi.jakojaannos.roguelite.game.view.systems.debug.EntityTransformRenderingSystem;

import static fi.jakojaannos.roguelite.engine.view.ui.ProportionValue.*;

public final class GameplayGameModeRenderer {
    public static final String TIME_PLAYED_LABEL_NAME = "time-played-timer";

    private static final String GAME_OVER_MESSAGE = "Game Over";
    private static final String GAME_OVER_HELP_TEXT = "Press <SPACE> to restart, <ESC> to return to menu";

    private GameplayGameModeRenderer() {
    }

    public static GameModeRenderer create(
            final Events events,
            final TimeManager timeManager,
            final Path assetRoot,
            final Camera camera,
            final AssetManager assetManager,
            final RenderingBackend backend,
            final AudioContext audioContext
    ) {
        final var userInterface = createUserInterface(events, timeManager, camera, assetManager);
        final var legacyDispatcher = createLegacyDispatcher(userInterface,
                                                            assetRoot,
                                                            camera,
                                                            assetManager,
                                                            backend,
                                                            audioContext);
        final var renderDispatcher = createRenderDispatcher(assetRoot,
                                                            assetManager,
                                                            backend,
                                                            camera);
        return new GameModeRenderer(legacyDispatcher,
                                    renderDispatcher,
                                    userInterface);
    }

    private static RenderDispatcher createRenderDispatcher(
            final Path assetRoot,
            final AssetManager assetManager,
            final RenderingBackend backend,
            final Camera camera
    ) {
        return RenderDispatcher.builder()
                               .withAdapter(new TurretAdapter(assetRoot,
                                                              backend,
                                                              assetManager.getAssetRegistry(Sprite.class),
                                                              camera))
                               .build();
    }

    private static SystemDispatcher createLegacyDispatcher(
            final UserInterface userInterface,
            final Path assetRoot,
            final Camera camera,
            final AssetManager assetManager,
            final RenderingBackend backend,
            final AudioContext audioContext
    ) {
        final var fontRegistry = assetManager.getAssetRegistry(Font.class);
        final var spriteRegistry = assetManager.getAssetRegistry(Sprite.class);
        final var textRenderer = backend.getTextRenderer();

        final var builder = SystemDispatcher.builder();
        final var level = builder.group("level")
                                 .withSystem(new LevelRenderingSystem(assetRoot, camera, spriteRegistry, backend))
                                 .buildGroup();

        final var entities = builder.group("entities")
                                    .withSystem(new SpriteRenderingSystem(assetRoot, camera, spriteRegistry, backend))
                                    .dependsOn(level)
                                    .buildGroup();

        final var ui = builder.group("ui")
                              .withSystem(new UpdateHUDSystem(userInterface))
                              .withSystem(new UpdateGameOverSplashSystem(userInterface))
                              .withSystem(new HealthBarUpdateSystem(camera, userInterface))
                              .withSystem(new NetworkHUDSystem(userInterface))
                              .withSystem(new UserInterfaceRenderingSystem(assetRoot,
                                                                           camera,
                                                                           fontRegistry,
                                                                           spriteRegistry,
                                                                           textRenderer,
                                                                           userInterface,
                                                                           backend))
                              .dependsOn(entities, level)
                              .buildGroup();
        final var sound = builder.group("sound")
                                 .withSystem(new HandleAudioEventsSystem(assetRoot, audioContext))
                                 .withSystem(new BackgroundMusicLoopSystem(assetRoot, audioContext))
                                 .dependsOn(ui)
                                 .buildGroup();

        if (DebugConfig.debugModeEnabled) {
            builder.group("debug")
                   .withSystem(new EntityCollisionBoundsRenderingSystem(assetRoot, camera, backend))
                   .withSystem(new EntityTransformRenderingSystem(assetRoot, camera, backend))
                   .dependsOn(ui, entities, level, sound)
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
        return UserInterface.builder(events, timeManager, camera.getViewport(), font)
                            .element(TIME_PLAYED_LABEL_NAME,
                                     GameplayGameModeRenderer::buildTimePlayedTimer)
                            .element("score-kills",
                                     GameplayGameModeRenderer::buildKillsCounter)
                            .element("game-over-container",
                                     GameplayGameModeRenderer::buildGameOverSplash)
                            .element("weapon-ammo",
                                     GameplayGameModeRenderer::buildAmmoCounter)
                            .element("weapon-heat",
                                     GameplayGameModeRenderer::buildHeatCounter)
                            .build();
    }

    private static void buildGameOverSplash(final UIElementBuilder builder) {
        builder.anchorY(percentOf(parentHeight(0.5)))
               .height(absolute(70))
               .left(absolute(0))
               .top(absolute(0))
               .width(percentOf(parentWidth(1.0)))
               .child("game-over-label",
                      label -> label.anchorX(percentOf(parentWidth(0.5)))
                                    .top(absolute(0))
                                    .left(percentOf(ownWidth(-0.5)))
                                    .text(GAME_OVER_MESSAGE)
                                    .fontSize(48))
               .child("game-over-help-label",
                      label -> label.anchorX(percentOf(parentWidth(0.5)))
                                    .bottom(absolute(0))
                                    .left(percentOf(ownWidth(-0.5)))
                                    .text(GAME_OVER_HELP_TEXT)
                                    .fontSize(24));
    }

    private static void buildHeatCounter(final UIElementBuilder builder) {
        builder.anchorX(absolute(0))
               .right(absolute(10))
               .bottom(absolute(45))
               .fontSize(36)
               .text("Heat")
               .color(1.0, 1.0, 1.0);
    }

    private static void buildAmmoCounter(final UIElementBuilder builder) {
        builder.anchorX(absolute(0))
               .right(absolute(10))
               .bottom(absolute(5))
               .fontSize(36)
               .text("4/20")
               .color(1.0, 1.0, 1.0);
    }

    private static void buildKillsCounter(final UIElementBuilder builder) {
        builder.anchorX(absolute(0))
               .left(absolute(5))
               .bottom(absolute(0))
               .fontSize(24)
               .text("Kills: ??");
    }

    private static void buildTimePlayedTimer(final UIElementBuilder builder) {
        builder.anchorX(percentOf(parentWidth(0.5)))
               .left(percentOf(ownWidth(-0.5)))
               .top(absolute(5))
               .fontSize(48)
               .text("12:34:56");
    }
}
