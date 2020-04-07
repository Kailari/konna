package fi.jakojaannos.roguelite.engine.view.systems.ui;

import java.util.stream.Stream;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.Requirements;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.Parent;
import fi.jakojaannos.roguelite.engine.view.data.resources.ui.UIHierarchy;

/**
 * Keeps the UI hierarchy up to date.
 */
public class UIHierarchySystem implements EcsSystem<UIHierarchySystem.Resources, EcsSystem.AllEntities, EcsSystem.NoEvents> {
    @Nullable
    public static EntityHandle getParent(final EntityHandle entity) {
        return entity.getComponent(Parent.class)
                     .map(parent -> parent.value)
                     .orElse(null);
    }

    @Override
    public Requirements<Resources, AllEntities, NoEvents> declareRequirements(
            final Requirements<Resources, AllEntities, NoEvents> require
    ) {
        return require.entityData(AllEntities.class)
                      .resources(Resources.class);
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<AllEntities>> entities,
            final NoEvents noEvents
    ) {
        final var hierarchy = resources.hierarchy;

        hierarchy.clear();
        entities.forEach(entityData -> hierarchy.update(entityData.getHandle(),
                                                        getParent(entityData.getHandle())));
    }

    // TODO: record EntityData(Optional<Parent> maybeParent) {}
    //  - Allows getting rid of ugly AllEntities workaround

    public static record Resources(
            UIHierarchy hierarchy
    ) {
    }
}
