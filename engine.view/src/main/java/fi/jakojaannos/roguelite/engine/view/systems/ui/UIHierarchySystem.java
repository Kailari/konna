package fi.jakojaannos.roguelite.engine.view.systems.ui;

import java.util.Optional;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.Parent;
import fi.jakojaannos.roguelite.engine.view.data.resources.ui.UIHierarchy;

/**
 * Keeps the UI hierarchy up to date.
 */
public class UIHierarchySystem implements EcsSystem<UIHierarchySystem.Resources, UIHierarchySystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var hierarchy = resources.hierarchy;

        hierarchy.clear();
        entities.forEach(entity -> hierarchy.update(entity.getHandle(),
                                                    entity.getData()
                                                          .parent()
                                                          .map(p -> p.value)
                                                          .orElse(null)));
    }

    public static record EntityData(Optional<Parent>parent) {}

    public static record Resources(UIHierarchy hierarchy) {}
}
