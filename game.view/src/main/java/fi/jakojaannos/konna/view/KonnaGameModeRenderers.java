package fi.jakojaannos.konna.view;

import fi.jakojaannos.konna.view.adapters.gameplay.*;
import fi.jakojaannos.konna.view.adapters.menu.MainMenuRenderAdapter;
import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.ecs.SystemDispatcher;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.riista.view.GameModeRenderers;
import fi.jakojaannos.riista.view.audio.AudioContext;
import fi.jakojaannos.roguelite.game.gamemode.GameplayGameMode;
import fi.jakojaannos.roguelite.game.gamemode.MainMenuGameMode;

public class KonnaGameModeRenderers {
    public static GameModeRenderers create(
            final AssetManager assetManager,
            final TimeManager timeManager,
            final AudioContext audioContext
    ) {
        final GameModeRenderers gameModeRenderers = new GameModeRenderers();

        // FIXME: Standardize adapter constructor arguments and load adapters from .json?
        //  - constructor args should be something like (assetManager, config)
        //  - config can be generic object of type TConfig which is _optionally_ populated if system
        //    overrides method getConfigClass to return non-null values. The parameter value can
        //    then be nullable
        //  - the config object is then consumed in the constructor to set values for the actual
        //    configuration fields (which should be final) on the adapter
        gameModeRenderers.register(GameplayGameMode.GAME_MODE_ID, gamePlay(assetManager, audioContext, timeManager));
        gameModeRenderers.register(MainMenuGameMode.GAME_MODE_ID, mainMenu(assetManager));

        return gameModeRenderers;
    }

    public static GameModeRenderers.Factory mainMenu(
            final AssetManager assetManager
    ) {
        return () -> {
            final var builder = SystemDispatcher.builder();
            builder.group("ui")
                   .withSystem(new MainMenuRenderAdapter(assetManager))
                   .buildGroup();

            return builder.build();
        };
    }

    public static GameModeRenderers.Factory gamePlay(
            final AssetManager assetManager,
            final AudioContext audioContext,
            final TimeManager timeManager
    ) {
        return () -> {
            final var builder = SystemDispatcher.builder();
            builder.group("debug")
                   .withSystem(new EntityTransformRenderAdapter())
                   .withSystem(new EntityColliderRenderAdapter())
                   .buildGroup();
            builder.group("entities")
                   .withSystem(new PlayerCharacterRenderAdapter(assetManager))
                   .withSystem(new FollowerRenderAdapter(assetManager))
                   .buildGroup();
            builder.group("level")
                   .withSystem(new TileMapRenderAdapter(assetManager))
                   .buildGroup();
            builder.group("level")
                   .withSystem(new MuzzleFlashParticleRenderAdapter(assetManager))
                   .buildGroup();
            builder.group("ui")
                   .withSystem(new CharacterHealthbarRenderAdapter(assetManager, timeManager.convertToTicks(5.0)))
                   .withSystem(new SessionStatsHudRenderAdapter(assetManager))
                   .withSystem(new GameOverSplashHudRenderAdapter(assetManager))
                   .withSystem(new HordeMessageHudRenderAdapter(assetManager, timeManager.convertToTicks(4.0)))
                   .withSystem(new WeaponHudRenderAdapter(assetManager))
                   .buildGroup();
            builder.group("audio")
                   .withSystem(new BackgroundMusicAdapter(assetManager.getRootPath(), audioContext))
                   .withSystem(new WeaponSoundEffectAdapter(assetManager.getRootPath(), audioContext))
                   .buildGroup();

            return builder.build();
        };
    }
}
