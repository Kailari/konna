package fi.jakojaannos.konna.view.adapters;

import java.util.function.Predicate;
import java.util.stream.Stream;

import fi.jakojaannos.konna.engine.view.EcsRenderAdapter;
import fi.jakojaannos.konna.engine.view.Renderer;
import fi.jakojaannos.konna.engine.view.ui.Border;
import fi.jakojaannos.konna.engine.view.ui.Colors;
import fi.jakojaannos.konna.engine.view.ui.Sides;
import fi.jakojaannos.konna.engine.view.ui.UiElement;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;

import static fi.jakojaannos.konna.engine.view.ui.UiUnit.*;

public class CharacterHealthbarRenderAdapter implements EcsRenderAdapter<CharacterHealthbarRenderAdapter.Resources, CharacterHealthbarRenderAdapter.EntityData> {
    private final long healthbarDurationInTicks;

    public CharacterHealthbarRenderAdapter(final long healthbarDurationInTicks) {
        this.healthbarDurationInTicks = healthbarDurationInTicks;
    }

    @Override
    public void draw(
            final Renderer renderer,
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final long accumulator
    ) {
        entities.filter(shouldRender(resources.timeManager))
                .forEach(entity -> {
                    // FIXME: Load layout from .json and call .draw() on all entities' elements
                    //  - provide some overload for "attaching" the given UI to the entity
                    final var root = (UiElement) null;//renderer.ui().getElementForEntity(entity.getHandle());
                    // TODO: entity element should have scree-space bounds derived from its AABB
                    //  - not all entities have AABBs
                    //  - where AABB exists, read the entity boundaries, multiply by MVP matrix and
                    //    there we have it, entity bounds flattened to screen-space
                    //  - convert from normalized screen-space coords to whatever coordinate system
                    //    the UI uses


                    if (!entity.hasComponent(PlayerTag.class)) {
                        root.getOrCreateChild("bounds-outline",
                                              child -> child.border(Sides.HORIZONTAL, Border.FULL, pixels(2))
                                                            .border(Sides.VERTICAL, Border.CORNERS, pixels(2))
                                                            .borderCornerSize(Sides.VERTICAL, percent(10))
                                                            .color(Colors.RED));
                    }

                    // FIXME: Healthbar width is currently dependent of entity orientation
                    //  - create centered parent element for the healthbar (with fixed `width`)
                    //  - apply the `right`-property trick to child element
                    final var healthbarElement = root.getOrCreateChild(
                            "health-bar",
                            child -> child.top(percent(100))
                                          .height(pixels(10)));

                    final var currentHealth = entity.getData().health.currentHealth;
                    final var maxHealth = entity.getData().health.maxHealth;

                    final var remainingMultiple = currentHealth / maxHealth;
                    healthbarElement.right(multiple(remainingMultiple));
                });
    }

    private Predicate<EntityDataHandle<EntityData>> shouldRender(final TimeManager timeManager) {
        return entity -> {
            final var health = entity.getData().health;
            final var isImportant = entity.hasComponent(PlayerTag.class);

            final var ticksSinceDamaged = timeManager.getCurrentGameTime() - health.lastDamageInstanceTimeStamp;
            return isImportant || ticksSinceDamaged < this.healthbarDurationInTicks;
        };
    }

    public static record Resources(
            CameraProperties cameraProperties,
            TimeManager timeManager
    ) {}

    public static record EntityData(
            Transform transform,
            Health health
    ) {}
}
