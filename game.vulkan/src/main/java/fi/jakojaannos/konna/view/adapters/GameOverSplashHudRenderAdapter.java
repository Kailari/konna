package fi.jakojaannos.konna.view.adapters;

import java.util.stream.Stream;

import fi.jakojaannos.konna.engine.assets.AssetManager;
import fi.jakojaannos.konna.engine.view.EcsRenderAdapter;
import fi.jakojaannos.konna.engine.view.Renderer;
import fi.jakojaannos.konna.engine.view.ui.UiElement;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.annotation.Without;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;

public class GameOverSplashHudRenderAdapter implements EcsRenderAdapter<EcsSystem.NoResources, GameOverSplashHudRenderAdapter.EntityData> {
    private final UiElement splash;

    public GameOverSplashHudRenderAdapter(final AssetManager assetManager) {
        this.splash = assetManager.getStorage(UiElement.class)
                                  .getOrDefault("ui/game-over.json");
    }

    @Override
    public void draw(
            final Renderer renderer,
            final EcsSystem.NoResources noResources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final long accumulator
    ) {
        if (entities.count() > 0) {
            return;
        }

        renderer.ui().draw(this.splash);
    }

    public static record EntityData(
            PlayerTag playerTag,
            @Without DeadTag deadTag
    ) {}
}
