package fi.jakojaannos.konna.view.adapters;

import java.util.stream.Stream;

import fi.jakojaannos.konna.engine.assets.AssetManager;
import fi.jakojaannos.konna.engine.view.Renderer;
import fi.jakojaannos.konna.engine.view.ui.UiElement;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.annotation.DisabledByDefault;
import fi.jakojaannos.roguelite.engine.ecs.annotation.EnableOn;
import fi.jakojaannos.roguelite.game.data.events.GameLostEvent;

@DisabledByDefault
public class GameOverSplashHudRenderAdapter implements EcsSystem<GameOverSplashHudRenderAdapter.Resources, EcsSystem.NoEntities, GameOverSplashHudRenderAdapter.EventData> {
    private final UiElement splash;

    public GameOverSplashHudRenderAdapter(final AssetManager assetManager) {
        this.splash = assetManager.getStorage(UiElement.class)
                                  .getOrDefault("ui/game-over.json");
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<NoEntities>> noEntities,
            final EventData eventData
    ) {
        resources.renderer.ui().draw(this.splash);
    }

    public static record Resources(Renderer renderer) {}

    public static record EventData(@EnableOn GameLostEvent gameLost) {}
}
