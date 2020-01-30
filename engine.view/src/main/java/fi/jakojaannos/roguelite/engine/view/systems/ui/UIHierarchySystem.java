package fi.jakojaannos.roguelite.engine.view.systems.ui;

import java.util.stream.Stream;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.Parent;
import fi.jakojaannos.roguelite.engine.view.data.resources.ui.UIHierarchy;

/**
 * Keeps the UI hierarchy up to date.
 */
public class UIHierarchySystem implements ECSSystem {
    @Nullable
    private static Entity getParent(final EntityManager entityManager, final Entity entity) {
        return entityManager.getComponentOf(entity, Parent.class)
                            .map(parent -> parent.value)
                            .orElse(null);
    }

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(UISystemGroups.PREPARATIONS)
                    .requireResource(UIHierarchy.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var hierarchy = world.getOrCreateResource(UIHierarchy.class);
        final var entityManager = world.getEntityManager();
        hierarchy.clear();
        entities.forEach(entity -> hierarchy.update(entityManager,
                                                    entity,
                                                    getParent(entityManager, entity)));
    }
}
