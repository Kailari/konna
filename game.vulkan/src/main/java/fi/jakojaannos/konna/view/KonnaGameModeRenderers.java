package fi.jakojaannos.konna.view;

import fi.jakojaannos.konna.view.adapters.gameplay.*;
import fi.jakojaannos.konna.view.adapters.menu.MainMenuRenderAdapter;
import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.riista.view.GameModeRenderers;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.game.gamemode.GameplayGameMode;
import fi.jakojaannos.roguelite.game.gamemode.MainMenuGameMode;

public class KonnaGameModeRenderers {
    public static GameModeRenderers create(
            final AssetManager assetManager,
            final TimeManager timeManager
    ) {
        final GameModeRenderers gameModeRenderers = new GameModeRenderers();
        gameModeRenderers.register(GameplayGameMode.GAME_MODE_ID, () -> {
            // FIXME: Standardize constructor arguments and load adapters from .json?
            //  - constructor args should be something like (assetManager, config)
            //  - config can be generic object of type TConfig which is _optionally_ populated if system
            //    overrides method getConfigClass to return non-null values. The parameter value can
            //    then be nullable
            //  - the config object is then consumed in the constructor to set values for the actual
            //    configuration fields (which should be final) on the adapter
            final var builder = SystemDispatcher.builder();
            builder.group("debug")
                   .withSystem(new EntityTransformRenderAdapter())
                   .withSystem(new EntityColliderRenderAdapter())
                   .buildGroup();
            builder.group("entities")
                   .withSystem(new PlayerCharacterRenderAdapter(assetManager))
                   .buildGroup();
            builder.group("ui")
                   //.withAdapter(new CharacterHealthbarRenderAdapter(timeManager.convertToTicks(5.0)))
                   .withSystem(new SessionStatsHudRenderAdapter(assetManager))
                   .withSystem(new GameOverSplashHudRenderAdapter(assetManager))
                   .withSystem(new HordeMessageHudRenderAdapter(assetManager, timeManager.convertToTicks(4.0)))
                   .buildGroup();

            return builder.build();
        });
        gameModeRenderers.register(MainMenuGameMode.GAME_MODE_ID, () -> {
            final var builder = SystemDispatcher.builder();
            builder.group("ui")
                   .withSystem(new MainMenuRenderAdapter(assetManager))
                   .buildGroup();

            return builder.build();
        });

        return gameModeRenderers;
    }
}
