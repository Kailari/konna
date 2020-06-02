package fi.jakojaannos.konna.view.adapters;

import java.util.stream.Stream;

import fi.jakojaannos.konna.engine.assets.AssetManager;
import fi.jakojaannos.konna.engine.view.EcsRenderAdapter;
import fi.jakojaannos.konna.engine.view.Renderer;
import fi.jakojaannos.konna.engine.view.ui.UiElement;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.resources.Horde;

public class HordeMessageHudRenderAdapter implements EcsRenderAdapter<HordeMessageHudRenderAdapter.Resources, EcsSystem.NoEntities> {
    private final UiElement splash;
    private final long messageDuration;

    public HordeMessageHudRenderAdapter(final AssetManager assetManager, final long messageDuration) {
        this.splash = assetManager.getStorage(UiElement.class)
                                  .getOrDefault("ui/horde-incoming.json");
        this.messageDuration = messageDuration;
    }

    @Override
    public void draw(
            final Renderer renderer,
            final Resources resources,
            final Stream<EntityDataHandle<EcsSystem.NoEntities>> noEntities,
            final long accumulator
    ) {
        final var currentTime = resources.timeManager.getCurrentGameTime();
        final var elapsed = currentTime - resources.horde.changeTimestamp;
        if (elapsed <= this.messageDuration) {
            renderer.ui().setValue("HORDE_MESSAGE", switch (resources.horde.status) {
                case ACTIVE -> "Wave #" + resources.horde.hordeIndex + " incoming";
                case ENDING -> "Kill all remaining enemies";
                case INACTIVE -> "Wave complete!";
            });

            renderer.ui().draw(this.splash);
        }
    }

    public static record Resources(
            TimeManager timeManager,
            Horde horde
    ) {}
}
