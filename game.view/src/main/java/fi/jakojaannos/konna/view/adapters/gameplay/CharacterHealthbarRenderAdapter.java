package fi.jakojaannos.konna.view.adapters.gameplay;

import java.util.stream.Stream;

import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.riista.view.ui.UiElement;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.ecs.annotation.Without;
import fi.jakojaannos.roguelite.game.data.components.NoDrawTag;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;

/**
 * Renders any entities with transform as debug transform handles.
 */
public class CharacterHealthbarRenderAdapter implements EcsSystem<CharacterHealthbarRenderAdapter.Resources, CharacterHealthbarRenderAdapter.EntityData, EcsSystem.NoEvents> {
    private final UiElement healthbar;
    private final long healthbarDurationInTicks;

    public CharacterHealthbarRenderAdapter(
            final AssetManager assetManager,
            final TimeManager timeManager
    ) {
        this.healthbarDurationInTicks = timeManager.convertToTicks(5.0);

        this.healthbar = assetManager.getStorage(UiElement.class)
                                     .getOrDefault("ui/healthbar.json");
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var renderer = resources.renderer;
        final var timeManager = resources.timeManager;

        entities.forEach(entity -> {
            final var transform = entity.getData().transform;
            final var health = entity.getData().health;

            final var isImportant = entity.hasComponent(PlayerTag.class);
            final var ticksSinceDamaged = timeManager.getCurrentGameTime() - health.lastDamageInstanceTimeStamp;
            if (!isImportant && ticksSinceDamaged >= this.healthbarDurationInTicks) {
                return;
            }

            // TODO: project entity coordinates to screen coordinates

            // FIXME: expose overload which allows specifying the area for the root
            renderer.ui().draw(this.healthbar);
        });
    }

    public static record Resources(
            Renderer renderer,
            TimeManager timeManager
    ) {}

    public static record EntityData(
            Transform transform,
            Health health,
            @Without NoDrawTag noDraw
    ) {}
}
