package fi.jakojaannos.konna.view.adapters.menu;

import java.util.stream.Stream;

import fi.jakojaannos.konna.engine.assets.AssetManager;
import fi.jakojaannos.konna.engine.view.Renderer;
import fi.jakojaannos.konna.engine.view.ui.UiElement;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;

public class MainMenuRenderAdapter implements EcsSystem<MainMenuRenderAdapter.Resources, EcsSystem.NoEntities, EcsSystem.NoEvents> {
    private final UiElement mainMenu;

    public MainMenuRenderAdapter(final AssetManager assetManager) {
        this.mainMenu = assetManager.getStorage(UiElement.class)
                               .getOrDefault("ui/main-menu.json");
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<NoEntities>> noEntities,
            final NoEvents noEvents
    ) {
        resources.renderer.ui().draw(this.mainMenu);
    }

    public static record Resources(Renderer renderer) {}
}
